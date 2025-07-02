grammar UserAgent;

@header { package app.revanced.extension.spotify; }

userAgent
    : product (WS product)* EOF
    ;

product
    : name ('/' version)? (WS comment)?
    ;

name
    : STRING
    ;

version
    : STRING ('.' STRING)*
    ;

comment
    : COMMENT
    ;

COMMENT
    : '(' ~ ')'* ')'
    ;

STRING
    : [a-zA-Z0-9]+
    ;

WS
    : [ \r\n]+
    ;
