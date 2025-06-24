grammar UserAgent;

@header { package app.revanced.extension.spotify; }

userAgent
    : (product WS (comment WS)?)* product (comment)? EOF
    ;

product
    : name ('/' version)?
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