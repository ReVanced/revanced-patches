package app.revanced.extension.spotify;

import org.antlr.v4.runtime.*;

public class UserAgent {
    private UserAgentParser.UserAgentContext ctx;

    public UserAgent(String userAgentString) {
        this.ctx = parse(userAgentString);
    }

    public UserAgent removeProduct(String name) {
        StringBuilder result = new StringBuilder();

        for (UserAgentParser.ProductContext product : ctx.product()) {
            if (product.name().getText().equals(name)) continue;

            result.append(product.getText()).append(" ");
        }

        ctx = parse(result.toString().trim());

        return this;
    }

    private static UserAgentParser.UserAgentContext parse(String userAgentString) {
        CharStream input = CharStreams.fromString(userAgentString);
        UserAgentLexer lexer = new UserAgentLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new UserAgentParser(tokens).userAgent();
    }

    @Override
    public String toString() {
        return ctx.getText();
    }
}
