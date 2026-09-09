package kr.co.csp.content.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.csp.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuestionTest {

    private static Question sample() {
        return Question.create(1L, 2, 3, "STM005", "정정보 해석", "본문");
    }

    @Test
    @DisplayName("modify는 회차까지 바꾼다 — PUT은 보낸 표현으로 전체를 치환한다")
    void modify_회차_반영() {
        Question question = sample();

        question.modify(9L, 4, 5, "RCC004", "새 제목", "새 본문");

        assertThat(question.getExamId()).isEqualTo(9L);
        assertThat(question.getSessionNo()).isEqualTo(4);
        assertThat(question.getQuestionNo()).isEqualTo(5);
        assertThat(question.getCategoryCode()).isEqualTo("RCC004");
        assertThat(question.getTitle()).isEqualTo("새 제목");
        assertThat(question.getContents()).isEqualTo("새 본문");
    }

    @Test
    @DisplayName("replaceContents는 본문만 바꾼다 — 도면 삭제 시 토큰 정리용 (DR-F01)")
    void replaceContents_본문만() {
        Question question = sample();

        question.replaceContents("토큰이 지워진 본문");

        assertThat(question.getContents()).isEqualTo("토큰이 지워진 본문");
        assertThat(question.getExamId()).isEqualTo(1L);
        assertThat(question.getSessionNo()).isEqualTo(2);
        assertThat(question.getQuestionNo()).isEqualTo(3);
        assertThat(question.getCategoryCode()).isEqualTo("STM005");
        assertThat(question.getTitle()).isEqualTo("정정보 해석");
    }

    @Test
    @DisplayName("삭제된 문항은 수정할 수 없다 (DR-L01)")
    void 삭제_문항_수정_불가() {
        Question question = sample();
        question.delete();

        assertThatThrownBy(() -> question.modify(1L, 2, 3, "STM005", "제목", "본문"))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> question.replaceContents("본문"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("삭제는 물리 삭제가 아니라 QST003 상태 전이다 (DR-L01)")
    void 삭제는_상태_전이() {
        Question question = sample();

        question.delete();

        assertThat(question.getStatusCode()).isEqualTo("QST003");
        assertThat(question.isDeleted()).isTrue();
        assertThat(question.isPublished()).isFalse();
    }
}
