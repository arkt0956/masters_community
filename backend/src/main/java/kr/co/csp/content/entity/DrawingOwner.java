package kr.co.csp.content.entity;

/**
 * 도면 소유자 (설계 3-13 · DR-F01).
 *
 * 소유자는 문항·해설·추가풀이 셋 중 정확히 하나다. DB의 ck_tb_csp_con04_owner가
 * 이를 강제하지만, 코드에서도 세 개의 nullable 필드를 직접 다루면 실수하기 쉬워
 * 하나의 값으로 묶었다.
 *
 * 다형 참조(owner_type + owner_id)를 쓰지 않은 이유는 FK를 걸 수 없어 무결성이
 * 애플리케이션 책임이 되기 때문이다. 물리 FK 3개를 유지한다.
 */
public record DrawingOwner(Type type, Long id) {

    public enum Type {
        QUESTION, SOLUTION, EXTRA_SOLUTION
    }

    public static DrawingOwner question(Long questionId) {
        return new DrawingOwner(Type.QUESTION, questionId);
    }

    public static DrawingOwner solution(Long solutionId) {
        return new DrawingOwner(Type.SOLUTION, solutionId);
    }

    public static DrawingOwner extraSolution(Long extraSolutionId) {
        return new DrawingOwner(Type.EXTRA_SOLUTION, extraSolutionId);
    }
}
