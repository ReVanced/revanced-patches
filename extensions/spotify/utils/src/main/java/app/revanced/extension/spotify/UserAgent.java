package app.revanced.extension.spotify;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class UserAgent {
    private final UserAgentParser.UserAgentContext tree;
    private final TokenStreamRewriter rewriter;
    private final ParseTreeWalker walker;

    public UserAgent(String userAgentString) {
        CharStream input = CharStreams.fromString(userAgentString);
        UserAgentLexer lexer = new UserAgentLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        tree = new UserAgentParser(tokens).userAgent();
        walker = new ParseTreeWalker();
        rewriter = new TokenStreamRewriter(tokens);
    }

    public UserAgent withoutProduct(String name) {
        walker.walk(new UserAgentBaseListener() {
            @Override
            public void exitProduct(UserAgentParser.ProductContext ctx) {
                if (!ctx.name().getText().contains(name)) return;

                int startIndex = ctx.getStart().getTokenIndex();
                if (startIndex != 0) startIndex -= 1; // Also remove the preceding whitespace.

                int stopIndex = ctx.getStop().getTokenIndex();

                
                rewriter.delete(startIndex, stopIndex);
            }
        }, tree);

        return new UserAgent(rewriter.getText().trim());
    }

    public UserAgent withCommentReplaced(String containing, String replacement) {
        walker.walk(new UserAgentBaseListener() {
            @Override
            public void exitComment(UserAgentParser.CommentContext ctx) {
                if (ctx.getText().contains(containing)) {
                    rewriter.replace(ctx.getStart(), ctx.getStop(), "(" + replacement + ")");
                }
            }
        }, tree);

        return new UserAgent(rewriter.getText());
    }

    @Override
    public String toString() {
        return rewriter.getText();
    }
}
