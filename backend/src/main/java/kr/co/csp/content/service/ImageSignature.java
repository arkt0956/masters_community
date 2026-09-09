package kr.co.csp.content.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import kr.co.csp.common.exception.DomainException;

/**
 * 업로드 파일 형식 판별 (DR-F02).
 *
 * 왜 확장자를 믿지 않는가: 확장자만 보면 이름만 .png로 바꾼 파일을 걸러낼 수 없다.
 * 실제 파일 시그니처(매직 넘버)로 판별하고, 그 결과를 file_type에 저장한다.
 *
 * file_type은 서빙 시 Content-Type이 된다. 저장 파일에 확장자가 없으므로
 * 이 값이 틀리면 브라우저가 이미지를 렌더링하지 않는다.
 */
public final class ImageSignature {

    /** 시그니처를 판별하기에 충분한 길이. SVG 판별을 위해 넉넉히 읽는다. */
    private static final int PROBE_SIZE = 512;

    private ImageSignature() {
    }

    /**
     * 스트림 앞부분을 들여다보고 MIME을 정한다.
     * mark/reset이 가능한 스트림을 넘겨야 한다. 판별 후 위치를 되돌린다.
     */
    public static String detect(InputStream in) {
        if (!in.markSupported()) {
            throw new IllegalArgumentException("mark를 지원하는 스트림이 필요합니다.");
        }
        byte[] head = new byte[PROBE_SIZE];
        int read;
        try {
            in.mark(PROBE_SIZE + 1);
            read = in.readNBytes(head, 0, PROBE_SIZE);
            in.reset();
        } catch (IOException e) {
            throw new DomainException("업로드 파일을 읽지 못했습니다.");
        }
        if (read <= 0) {
            throw new DomainException("빈 파일은 등록할 수 없습니다.");
        }
        byte[] probe = Arrays.copyOf(head, read);

        if (startsWith(probe, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "image/png";
        }
        if (startsWith(probe, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        // WEBP: "RIFF" .... "WEBP"
        if (startsWith(probe, 'R', 'I', 'F', 'F') && read >= 12
                && probe[8] == 'W' && probe[9] == 'E' && probe[10] == 'B' && probe[11] == 'P') {
            return "image/webp";
        }
        if (isSvg(probe)) {
            // SVG는 텍스트라 스크립트를 담을 수 있다. 서빙 시 Content-Disposition과
            // CSP로 스크립트 실행을 막는 것이 전제다 (DrawingController).
            return "image/svg+xml";
        }
        throw new DomainException("PNG · JPEG · WEBP · SVG 파일만 등록할 수 있습니다.");
    }

    private static boolean isSvg(byte[] probe) {
        String head = new String(probe, java.nio.charset.StandardCharsets.UTF_8)
                .stripLeading().toLowerCase();
        return head.startsWith("<svg") || head.startsWith("<?xml") && head.contains("<svg");
    }

    private static boolean startsWith(byte[] probe, int... signature) {
        if (probe.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((probe[i] & 0xFF) != (signature[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }
}
