grammar UserAgent;

@header { package app.revanced.extension.spotify; }

userAgent
    : product+ EOF
    ;

product
    : name ('/' version)? COMMENT?
    ;

name
    : STRING
    ;

version
    : STRING ('.' STRING)*
    ;

COMMENT
    : '(' ~ ')'* ')'
    ;

STRING
    : [a-zA-Z0-9]+
    ;

WS
    : [ \r\n]+ -> skip
    ;