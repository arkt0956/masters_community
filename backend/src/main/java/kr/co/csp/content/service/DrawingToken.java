package kr.co.csp.content.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kr.co.csp.common.exception.DomainException;

/**
 * 본문 안의 이미지 위치 토큰 {@code [[drawing:n]]} (DR-F03).
 *
 * n은 drawing_no다. 도면을 삭제해도 재채번하지 않으므로(DR-F01) 번호는 연속이 아닐 수 있다.
 * 번호를 순서로 해석하지 않는다. 표시 순서는 토큰이 놓인 위치가 정한다.
 */
public final class DrawingToken {

    private static final Pattern TOKEN = Pattern.compile("\\[\\[drawing:(\\d+)]]");

    private DrawingToken() {
    }

    public static List<Integer> parse(String contents) {
        Matcher m = TOKEN.matcher(contents);
        List<Integer> found = new ArrayList<>();
        while (m.find()) {
            found.add(Integer.parseInt(m.group(1)));
        }
        return found;
    }

    /**
     * 게시 직전에만 호출한다. 작성중 저장에서는 호출하지 않는다.
     *
     * 왜 작성중에는 검증하지 않는가: 이미지를 먼저 올리고 토큰을 나중에 넣게 되므로,
     * 작성 도중에는 고아 토큰·미참조 도면이 정상적으로 존재한다.
     *
     * 왜 미참조 도면도 막는가: 통과시키면 올린 이미지가 화면에 나타나지 않는다.
     * 본문 끝에 자동으로 붙이는 방식은 쓰지 않는다. 의도하지 않은 위치에
     * 조용히 나타나는 편이 더 나쁘다.
     */
    public static void validateForPublish(String contents, Set<Integer> drawingNos) {
        List<Integer> tokens = parse(contents);
        Set<Integer> unique = new HashSet<>(tokens);

        if (tokens.size() != unique.size()) {
            throw new DomainException("같은 도면을 본문에서 두 번 이상 참조할 수 없습니다.");
        }
        Set<Integer> orphan = new HashSet<>(unique);
        orphan.removeAll(drawingNos);
        if (!orphan.isEmpty()) {
            throw new DomainException("본문이 참조하는 도면이 없습니다: " + orphan);
        }
        Set<Integer> unused = new HashSet<>(drawingNos);
        unused.removeAll(unique);
        if (!unused.isEmpty()) {
            throw new DomainException("본문에서 참조되지 않은 도면이 있습니다: " + unused);
        }
    }

    /**
     * 도면을 지울 때 본문의 해당 토큰만 함께 지운다 (DR-F01).
     *
     * 왜 지우는가: 토큰만 남으면 게시 시점 검증에서 고아 토큰으로 걸려 게시가 막힌다.
     * 삭제 시점에 정리해야 관리자가 원인을 찾지 않아도 된다.
     *
     * 번호는 당기지 않는다. 남은 도면의 번호와 토큰은 그대로 둔다.
     */
    public static String removeToken(String contents, int drawingNo) {
        // 토큰이 한 줄을 통째로 차지하는 경우 빈 줄이 남지 않도록 줄바꿈까지 함께 지운다.
        return contents.replaceAll("\\[\\[drawing:" + drawingNo + "]]\\R?", "");
    }
}
