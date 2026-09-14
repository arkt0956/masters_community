package kr.co.csp.participation.service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import kr.co.csp.common.config.CspProperties;
import kr.co.csp.content.service.DrawingService;
import kr.co.csp.content.service.DrawingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 도면 파일 정리 (DR-F02 보관 기간).
 *
 * 두 가지를 한다:
 *   ① 보관 기간이 지난 반려 추가풀이를 지운다 — 정책
 *   ② 참조되지 않는 파일을 지운다 — 안전망
 *
 * ②가 따로 필요한 이유는 ①의 파일 삭제가 실패할 수 있고, 롤백 사고로 DB 행 없이 파일만
 * 남을 수도 있기 때문이다. 어떤 경로로 생겼든 참조되지 않는 파일은 ②가 회수한다.
 *
 * 왜 트랜잭션을 이 클래스에 걸지 않는가:
 * DB를 먼저 커밋하고 파일을 그 뒤에 지워야 한다. 파일을 먼저 지우면 롤백됐을 때
 * DB에는 행이 있는데 파일이 없는 상태가 남는다. purgeRejected가 트랜잭션 경계이고,
 * 그 메서드가 반환한 뒤(=커밋된 뒤) 이 클래스가 파일을 지운다.
 */
@Component
public class DrawingCleaner {

    private static final Logger log = LoggerFactory.getLogger(DrawingCleaner.class);

    private final ExtraSolutionService extraSolutionService;
    private final DrawingService drawingService;
    private final DrawingStorage storage;
    private final CspProperties properties;

    public DrawingCleaner(ExtraSolutionService extraSolutionService, DrawingService drawingService,
                          DrawingStorage storage, CspProperties properties) {
        this.extraSolutionService = extraSolutionService;
        this.drawingService = drawingService;
        this.storage = storage;
        this.properties = properties;
    }

    /**
     * 매일 새벽 4시 (KST).
     *
     * 서비스 이용이 가장 적은 시간대다. 파일 시스템을 훑는 작업이라 사용자 요청과
     * 겹치지 않게 둔다. 시간대를 명시하지 않으면 JVM 기본값을 따라 배포 환경에 따라
     * 도는 시각이 달라진다.
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void clean() {
        purgeRejectedExtraSolutions();
        purgeOrphanFiles();
    }

    /** ① 보관 기간이 지난 반려 추가풀이 — 도면 파일·도면 행·추가풀이 행을 함께 지운다. */
    private void purgeRejectedExtraSolutions() {
        OffsetDateTime before = OffsetDateTime.now().minusDays(properties.retention().rejectedDays());
        try {
            List<UUID> fileUuids = extraSolutionService.purgeRejected(before);
            // 여기까지 왔다면 DB는 커밋됐다. 이제 파일을 지운다.
            fileUuids.forEach(storage::delete);
        } catch (RuntimeException e) {
            // 다음 실행이 다시 시도한다. 기동이나 다른 작업을 막지 않는다.
            log.error("반려 추가풀이 정리에 실패했습니다.", e);
        }
    }

    /** ② 참조되지 않는 파일 — ①이 놓쳤거나 롤백으로 남은 파일을 회수한다. */
    private void purgeOrphanFiles() {
        Instant before = Instant.now().minus(Duration.ofHours(properties.retention().orphanFileHours()));
        try {
            int deleted = drawingService.purgeOrphanFiles(before);
            if (deleted > 0) {
                log.info("참조되지 않는 도면 파일을 지웠습니다: {}개", deleted);
            }
        } catch (RuntimeException e) {
            log.error("고아 파일 청소에 실패했습니다.", e);
        }
    }
}
