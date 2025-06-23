package app.revanced.extension.spotify;

import org.antlr.v4.runtime.*;

public class UserAgent {
    public static void main(String[] args) {
        UserAgent ua = new UserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/137.0.7151.72 Mobile Safari/537.36");
        System.out.println(ua.withoutMobileProduct());
    }

    private final UserAgentParser.UserAgentContext ctx;

    public UserAgent(String userAgent) {
        CharStream input = CharStreams.fromString(userAgent);
        UserAgentLexer lexer = new UserAgentLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ctx = new UserAgentParser(tokens).userAgent();
    }

    public String withoutMobileProduct() {
        UserAgentVisitor<String> visitor = new UserAgentBaseVisitor<String>() {
            @Override
            public String visitUserAgent(UserAgentParser.UserAgentContext ctx) {
                StringBuilder result = new StringBuilder();
                for (UserAgentParser.ProductContext product : ctx.product()) {
                    if (!product.name().getText().equalsIgnoreCase("Mobile")) {
                        result.append(product.getText()).append(" ");
                    }
                }
                return result.toString().trim();
            }

            @Override
            public String visitProduct(UserAgentParser.ProductContext ctx) {
                return ctx.getText();
            }
        };
        
        return visitor.visit(ctx);
    }

    public String toString() {
        return ctx.getText();
    }
}
