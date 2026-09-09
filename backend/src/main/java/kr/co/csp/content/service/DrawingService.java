package kr.co.csp.content.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.csp.common.exception.DomainException;
import kr.co.csp.common.exception.NotFoundException;
import kr.co.csp.content.entity.Drawing;
import kr.co.csp.content.entity.DrawingOwner;
import kr.co.csp.content.entity.Question;
import kr.co.csp.content.entity.Solution;
import kr.co.csp.content.repository.DrawingRepository;
import kr.co.csp.content.repository.QuestionRepository;
import kr.co.csp.content.repository.SolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** 도면 등록·삭제 (DR-F01 · DR-F02 · DR-F03). */
@Service
public class DrawingService {

    private final DrawingRepository drawingRepository;
    private final QuestionRepository questionRepository;
    private final SolutionRepository solutionRepository;
    private final DrawingStorage storage;

    public DrawingService(DrawingRepository drawingRepository, QuestionRepository questionRepository,
                          SolutionRepository solutionRepository, DrawingStorage storage) {
        this.drawingRepository = drawingRepository;
        this.questionRepository = questionRepository;
        this.solutionRepository = solutionRepository;
        this.storage = storage;
    }

    /**
     * 도면 추가 (DR-F01).
     *
     * 왜 빈 번호를 메우지 않는가:
     * 삭제로 1, 3이 남아 있으면 다음은 4다. 2를 재사용하면 예전에
     * 삭제된 도면을 가리키던 토큰이 되살아나 다른 이미지를 보여준다.
     */
    @Transactional
    public Drawing addDrawing(DrawingOwner owner, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainException("도면 파일을 선택해 주세요.");
        }
        String fileType;
        DrawingStorage.Stored stored;
        try (InputStream raw = file.getInputStream();
             InputStream in = new BufferedInputStream(raw)) {
            // 확장자가 아니라 실제 시그니처로 판별한다 (DR-F02).
            fileType = ImageSignature.detect(in);
            stored = storage.store(in);
        } catch (IOException e) {
            throw new DomainException("도면 파일을 읽지 못했습니다.");
        }

        int nextNo = maxDrawingNo(owner) + 1;
        String fileName = file.getOriginalFilename() == null ? "drawing" : file.getOriginalFilename();
        try {
            return drawingRepository.save(Drawing.of(owner, nextNo, stored.fileUuid(),
                    fileName, stored.fileHash(), fileType));
        } catch (RuntimeException e) {
            // 파일 저장 → DB 커밋 → 실패 시 파일 삭제 (DR-P03)
            storage.delete(stored.fileUuid());
            throw e;
        }
    }

    /**
     * 도면 삭제 (DR-F01).
     *
     * 왜 토큰을 함께 지우는가:
     * 토큰만 남으면 게시 시점 검증(DR-F03)에서 고아 토큰으로 걸려 게시가 막힌다.
     * 삭제 시점에 정리해야 관리자가 원인을 찾지 않아도 된다.
     *
     * 번호는 당기지 않는다. 남은 도면의 번호는 그대로 둔다.
     */
    @Transactional
    public void deleteDrawing(Long drawingId) {
        Drawing target = drawingRepository.findById(drawingId)
                .orElseThrow(() -> new NotFoundException("도면을 찾을 수 없습니다."));
        DrawingOwner owner = target.owner();

        switch (owner.type()) {
            case QUESTION -> {
                Question question = questionRepository.findById(owner.id())
                        .orElseThrow(() -> new NotFoundException("문항을 찾을 수 없습니다."));
                question.replaceContents(
                        DrawingToken.removeToken(question.getContents(), target.getDrawingNo()));
            }
            case SOLUTION -> {
                Solution solution = solutionRepository.findById(owner.id())
                        .orElseThrow(() -> new NotFoundException("해설을 찾을 수 없습니다."));
                solution.modify(DrawingToken.removeToken(solution.getContents(), target.getDrawingNo()));
            }
            // 추가풀이는 등록 후 수정·삭제할 수 없다 (SCR-006 ③). 관리자도 본문을 고치지 않고
            // 게시 또는 반려만 한다 (SCR-A03). 그래서 도면만 지우는 경로를 두지 않는다.
            case EXTRA_SOLUTION -> throw new DomainException("추가풀이의 도면은 삭제할 수 없습니다.");
        }

        drawingRepository.delete(target);
        storage.delete(target.getFileUuid());
    }

    @Transactional(readOnly = true)
    public List<Drawing> findByOwner(DrawingOwner owner) {
        return switch (owner.type()) {
            case QUESTION -> drawingRepository.findByQuestionIdOrderByDrawingNoAsc(owner.id());
            case SOLUTION -> drawingRepository.findBySolutionIdOrderByDrawingNoAsc(owner.id());
            case EXTRA_SOLUTION -> drawingRepository.findByExtraSolutionIdOrderByDrawingNoAsc(owner.id());
        };
    }

    /** 게시 검증에 쓰는 도면 번호 집합 (DR-F03). */
    @Transactional(readOnly = true)
    public Set<Integer> drawingNos(DrawingOwner owner) {
        return findByOwner(owner).stream().map(Drawing::getDrawingNo).collect(Collectors.toSet());
    }

    private int maxDrawingNo(DrawingOwner owner) {
        return switch (owner.type()) {
            case QUESTION -> drawingRepository.findMaxNoByQuestionId(owner.id()).orElse(0);
            case SOLUTION -> drawingRepository.findMaxNoBySolutionId(owner.id()).orElse(0);
            case EXTRA_SOLUTION -> drawingRepository.findMaxNoByExtraSolutionId(owner.id()).orElse(0);
        };
    }
}
