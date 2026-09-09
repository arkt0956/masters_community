package kr.co.csp.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import kr.co.csp.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** DR-F03 — 본문 이미지 토큰. */
class DrawingTokenTest {

    @Nested
    @DisplayName("parse")
    class Parse {

        @Test
        @DisplayName("번호가 연속이 아니어도 등장 순서대로 읽는다 (DR-F01 재채번 금지)")
        void 불연속_번호() {
            String contents = """
                    다음 그림과 같은 단순보에 등분포하중이 작용한다.

                    [[drawing:1]]

                    이때 최대 휨모멘트를 구하라.

                    [[drawing:4]]
                    """;

            assertThat(DrawingToken.parse(contents)).containsExactly(1, 4);
        }

        @Test
        @DisplayName("토큰이 없으면 빈 목록")
        void 토큰_없음() {
            assertThat(DrawingToken.parse("도면 없는 문항입니다.")).isEmpty();
        }
    }

    @Nested
    @DisplayName("validateForPublish")
    class ValidateForPublish {

        @Test
        @DisplayName("토큰과 도면이 정확히 대응하면 통과")
        void 정상() {
            assertThatCode(() ->
                    DrawingToken.validateForPublish("앞[[drawing:1]]뒤[[drawing:3]]", Set.of(1, 3)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("고아 토큰이면 게시를 막는다")
        void 고아_토큰() {
            assertThatThrownBy(() ->
                    DrawingToken.validateForPublish("[[drawing:2]]", Set.of(1)))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("본문이 참조하는 도면이 없습니다");
        }

        @Test
        @DisplayName("미참조 도면이면 게시를 막는다 — 올린 이미지가 화면에 안 나타나기 때문")
        void 미참조_도면() {
            assertThatThrownBy(() ->
                    DrawingToken.validateForPublish("[[drawing:1]]", Set.of(1, 2)))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("참조되지 않은 도면");
        }

        @Test
        @DisplayName("같은 번호를 두 번 참조하면 게시를 막는다")
        void 토큰_중복() {
            assertThatThrownBy(() ->
                    DrawingToken.validateForPublish("[[drawing:1]] 그리고 [[drawing:1]]", Set.of(1)))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("두 번 이상");
        }

        @Test
        @DisplayName("도면도 토큰도 없으면 통과")
        void 둘_다_없음() {
            assertThatCode(() -> DrawingToken.validateForPublish("본문만 있습니다.", Set.of()))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("removeToken")
    class RemoveToken {

        @Test
        @DisplayName("지운 도면의 토큰만 없애고 다른 번호는 건드리지 않는다 (DR-F01)")
        void 해당_토큰만_제거() {
            String contents = "앞\n[[drawing:1]]\n중간\n[[drawing:2]]\n뒤";

            String result = DrawingToken.removeToken(contents, 1);

            assertThat(result).doesNotContain("[[drawing:1]]");
            assertThat(result).contains("[[drawing:2]]");
        }
    }
}
