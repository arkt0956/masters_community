package kr.co.csp.content.dto;

import java.util.UUID;
import kr.co.csp.content.entity.Drawing;

/**
 * 도면 응답.
 *
 * 파일 경로가 아니라 file_uuid를 내려준다. 프론트는 /api/drawings/{fileUuid}로 받는다 (R-53).
 * file_hash는 내려주지 않는다. 화면이 쓰지 않고, 원본 대조는 서버가 할 일이다.
 */
public record DrawingResponse(
        Long drawingId,
        int drawingNo,
        UUID fileUuid,
        String fileName,
        String fileType
) {

    public static DrawingResponse from(Drawing drawing) {
        return new DrawingResponse(drawing.getDrawingId(), drawing.getDrawingNo(),
                drawing.getFileUuid(), drawing.getFileName(), drawing.getFileType());
    }
}
