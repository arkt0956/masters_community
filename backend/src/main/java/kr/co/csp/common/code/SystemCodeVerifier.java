package kr.co.csp.common.code;

import java.util.List;
import kr.co.csp.common.code.SystemCode.EsStatus;
import kr.co.csp.common.code.SystemCode.Grp;
import kr.co.csp.common.code.SystemCode.QStatus;
import kr.co.csp.common.code.SystemCode.RptStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 시스템 코드가 DB에 실제로 있는지 기동 시 확인한다 (DR-C05).
 *
 * 왜 ApplicationRunner인가:
 * 여기서 예외를 던지면 기동이 실패한다. 서비스가 뜬 뒤 조용히
 * 오작동하는 것보다 배포 시점에 실패하는 편이 낫다.
 */
@Component
public class SystemCodeVerifier implements ApplicationRunner {

    private final CodeRegistry codeRegistry;

    private static final List<String[]> REQUIRED = List.of(
            new String[]{Grp.Q_STATUS, QStatus.DRAFT},
            new String[]{Grp.Q_STATUS, QStatus.PUBLISHED},
            new String[]{Grp.Q_STATUS, QStatus.DELETED},
            new String[]{Grp.RPT_TYPE, "RPT001"},
            new String[]{Grp.RPT_TYPE, "RPT002"},
            new String[]{Grp.RPT_TYPE, "RPT003"},
            new String[]{Grp.RPT_STATUS, RptStatus.RECEIVED},
            new String[]{Grp.RPT_STATUS, RptStatus.REVIEWING},
            new String[]{Grp.RPT_STATUS, RptStatus.APPLIED},
            new String[]{Grp.RPT_STATUS, RptStatus.REJECTED},
            new String[]{Grp.ES_STATUS, EsStatus.WAITING},
            new String[]{Grp.ES_STATUS, EsStatus.REVIEWING},
            new String[]{Grp.ES_STATUS, EsStatus.PUBLISHED},
            new String[]{Grp.ES_STATUS, EsStatus.REJECTED}
    );

    public SystemCodeVerifier(CodeRegistry codeRegistry) {
        this.codeRegistry = codeRegistry;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String[] pair : REQUIRED) {
            if (!codeRegistry.exists(pair[0], pair[1])) {
                throw new IllegalStateException("필수 공통코드 누락: " + pair[0] + "/" + pair[1]);
            }
        }
    }
}
