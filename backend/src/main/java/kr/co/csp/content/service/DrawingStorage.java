package kr.co.csp.content.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import kr.co.csp.common.config.CspProperties;
import kr.co.csp.common.exception.DomainException;
import kr.co.csp.common.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 도면 원본 파일 저장 (DR-F02 · R-16 · R-53).
 *
 * 저장 파일명은 file_uuid다. 원본 파일명을 경로에 쓰면 이름을 추측해 다른 파일에
 * 접근할 수 있다. 원본 파일명은 표시용으로만 DB에 둔다.
 *
 * 바이트를 그대로 옮겨 담는다. 리사이즈·재인코딩을 하지 않으므로 등록 시점의 해시가
 * 언제나 저장 파일의 해시와 같다 (R-16).
 */
@Component
public class DrawingStorage {

    private static final Logger log = LoggerFactory.getLogger(DrawingStorage.class);

    private final Path root;

    public DrawingStorage(CspProperties properties) {
        this.root = Path.of(properties.drawingDir()).toAbsolutePath().normalize();
    }

    public record Stored(UUID fileUuid, String fileHash, long size) {
    }

    /**
     * 파일을 저장하고 해시를 함께 돌려준다.
     *
     * 왜 저장과 해시를 한 메서드에서 하는가: 두 번 읽으면 그 사이에 내용이 달라질 여지가
     * 생긴다. 임시 파일에 쓰면서 해시를 계산하고, 끝난 뒤 최종 이름으로 옮긴다.
     */
    public Stored store(InputStream in) {
        UUID fileUuid = UUID.randomUUID();
        try {
            Files.createDirectories(root);
            Path target = resolve(fileUuid);
            Path temp = Files.createTempFile(root, "upload-", ".part");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long size;
            try (var out = Files.newOutputStream(temp)) {
                byte[] buffer = new byte[8192];
                int read;
                long total = 0;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                    out.write(buffer, 0, read);
                    total += read;
                }
                size = total;
            }
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            return new Stored(fileUuid, HexFormat.of().formatHex(digest.digest()), size);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        } catch (IOException e) {
            throw new DomainException("도면 파일을 저장하지 못했습니다.");
        }
    }

    public Path path(UUID fileUuid) {
        Path path = resolve(fileUuid);
        if (!Files.exists(path)) {
            throw new NotFoundException("도면 파일을 찾을 수 없습니다.");
        }
        return path;
    }

    /**
     * 파일 삭제.
     *
     * 순서는 "파일 저장 → DB 커밋 → 실패 시 파일 삭제"다 (DR-P03).
     * DB보다 파일을 먼저 지우면 롤백됐을 때 DB에는 있는데 파일이 없는 행이 남는다.
     *
     * 실패해도 예외를 던지지 않는다. 두 가지 이유다:
     *   · 트랜잭션 안에서 호출되면 예외가 DB 삭제까지 되돌린다. 파일 하나 못 지운 것 때문에
     *     되돌리면 남는 것은 "지워야 할 행 + 지워야 할 파일"이라 더 나쁘다.
     *   · addDrawing의 보상 경로에서 호출되는데, 여기서 던지면 원래 예외가 가려진다.
     * 남은 파일은 고아 파일 청소가 회수한다 (DR-F02).
     */
    public void delete(UUID fileUuid) {
        try {
            Files.deleteIfExists(resolve(fileUuid));
        } catch (IOException | DomainException e) {
            log.warn("도면 파일을 삭제하지 못했습니다. 고아 파일 청소 대상으로 남습니다: {}", fileUuid, e);
        }
    }

    /**
     * 저장 디렉터리의 파일 목록 (DR-F02 고아 파일 청소).
     *
     * 업로드 중 임시 파일(upload-*.part)과 이름이 UUID가 아닌 파일은 빼고 돌려준다.
     * 임시 파일까지 넘기면 업로드가 진행 중인 파일을 지울 수 있다.
     *
     * modifiedBefore보다 나중에 수정된 파일도 뺀다. 방금 올라와 아직 커밋되지 않은
     * 파일은 DB에서 찾을 수 없어 고아로 보이기 때문이다.
     */
    public List<UUID> listFileUuids(Instant modifiedBefore) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(root)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> isOlderThan(path, modifiedBefore))
                    .map(path -> parseUuid(path.getFileName().toString()))
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            log.warn("도면 저장 디렉터리를 읽지 못했습니다: {}", root, e);
            return List.of();
        }
    }

    private boolean isOlderThan(Path path, Instant threshold) {
        try {
            return Files.getLastModifiedTime(path).toInstant().isBefore(threshold);
        } catch (IOException e) {
            // 시각을 못 읽으면 지우지 않는다. 판단이 서지 않을 때는 남기는 쪽이 안전하다.
            return false;
        }
    }

    private static UUID parseUuid(String fileName) {
        try {
            return UUID.fromString(fileName);
        } catch (IllegalArgumentException e) {
            return null;   // upload-*.part 등 우리가 만든 이름이 아닌 파일
        }
    }

    /**
     * UUID.toString()은 하이픈과 16진수뿐이라 경로 이탈 문자가 들어갈 수 없다.
     * 그래도 normalize 후 root 하위인지 확인한다.
     */
    private Path resolve(UUID fileUuid) {
        Path path = root.resolve(fileUuid.toString()).normalize();
        if (!path.startsWith(root)) {
            throw new DomainException("잘못된 파일 경로입니다.");
        }
        return path;
    }
}
