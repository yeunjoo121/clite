import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) { // * return the string of a token if it matches with t *
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
        //left brace와 rightbrace 사이에는 declarations와 statements가 있어야 한다.
        //declarations 추가
        Declarations decs = declarations();
        Block b = programStartStatements();
        // student exercise
        match(TokenType.RightBrace);
        //return program_node 추가
        return new Program(decs, b);  // student exercise
    }
  
    private Declarations declarations () {
        // Declarations --> { Declaration }
        // {Declaration} 추가
        Declarations decs = new Declarations();
        while (isType())
        {
            declaration(decs);
        }
        //return Declaration_node 추가
        return decs;  // student exercise
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        // student exercise
        //var 생성 후 이 var이 identifier인지 구분. 또 , 있으면 계속 받을 수 있게 함.
        Variable var;
        Declaration dec;
        Type t = type();
        var = new Variable(match(TokenType.Identifier));
        dec = new Declaration(var, t);
        ds.add(dec);
        while (isComma())
        {
            token = lexer.next();
            var = new Variable(match(TokenType.Identifier));
            dec = new Declaration(var, t);

            ds.add(dec);
        }
        match(TokenType.Semicolon);
    }
  
    private Type type () {
        // Type  -->  int | bool | float | char 
        //type을 리턴하는 함수. 이 위의 4개의 타입이 아니면 에러.
        Type t = null;
        if (token.type().equals(TokenType.Int))
        {
            t = Type.INT;
        }
        else if (token.type().equals(TokenType.Bool))
        {
            t = Type.BOOL;
        }
        else if (token.type().equals(TokenType.Float))
        {
            t = Type.FLOAT;
        }
        else if (token.type().equals(TokenType.Char))
        {
            t = Type.CHAR;
        }
        else error("Type error");
        token = lexer.next();
        // student exercise
        return t;          
    }
  
    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = null;
        // student exercise

        if (token.type().equals(TokenType.Semicolon))
        {
            //세미콜론일때는 skip함. 아무것도 없이 ;;;;이것만 있는 거라서
            s = new Skip();
            token = lexer.next();
        }
        else if (token.type().equals(TokenType.LeftBrace))
        {
            //{이게 있는 거면 block
            s = statements();
        }
        else if (token.type().equals(TokenType.Identifier))
        {
            //id 있으면 assignment
            s = assignment();
        }
        else if (token.type().equals(TokenType.If))
        {
            //if있으면 ifStatement
            s = ifStatement();
        }
        else if (token.type().equals(TokenType.While))
        {
            //while 있으면 while statement
            s = whileStatement();
        }
        else error("Statement error");
        return s;
    }
  
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        //statement 선언
        Statement s;
        // student exercise
        // { 있는지 확인 후 { 뒤에 statements 계속 오면 계속 실행
        match(TokenType.LeftBrace);
        while (isStatement())
        {
            s = statement();
            b.members.add(s);
        }
        //}있는지 확인
        match(TokenType.RightBrace);
        return b;
    }

    //programStartStatements 추가
    private Block programStartStatements()
    {
        // Block --> '{' Statements '}'
        Block b = new Block();
        Statement s;
        while (isStatement())
        {
            s = statement();
            b.members.add(s);
        }
        return b;
    }

    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
        // identifier = expression ; 모두 있는지 확인
        Variable var;
        Expression e;
        
        var = new Variable(match(TokenType.Identifier));
        match(TokenType.Assign);
        e = expression();
        match(TokenType.Semicolon);
        return new Assignment(var, e);  // student exercise
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        Conditional c;
        Statement s;
        Expression exp;

        match(TokenType.If);
        match(TokenType.LeftParen);
        exp = expression();
        match(TokenType.RightParen);
        s = statement();

        //else
        if (token.type().equals(TokenType.Else))
        {
            token = lexer.next();
            Statement elsestatement = statement();
            c = new Conditional(exp, s, elsestatement);
        }
        else
        {
            c = new Conditional(exp, s);
        }
        return c;  // student exercise
    }
  
    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        Statement s;
        Expression exp;

        match(TokenType.While);
        match(TokenType.LeftParen);
        exp = expression();
        match(TokenType.RightParen);
        s = statement();
        return new Loop(exp, s);  // student exercise
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
        Expression conj = conjunction();
        while (token.type().equals(TokenType.Or))
        {
            Operator op = new Operator(match(token.type()));
            Expression exp = expression();
            conj = new Binary(op, conj, exp);
        }
        return conj;  // student exercise
    }
  
    private Expression conjunction() {
        // Conjunction --> Equality { && Equality }
        Expression equal = equality();
        while (token.type().equals(TokenType.And))
        {
            Operator op = new Operator(match(token.type()));
            Expression exp = conjunction();
            equal = new Binary(op, equal, exp);
        }
        return equal;  // student exercise
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression exp = relation();
        while (isEqualityOp())
        {
            Operator op = new Operator(match(token.type()));
            Expression term2 = relation();
            exp = new Binary(op, exp, term2);
        }
        return exp;  // student exercise
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition] 
        Expression exp = addition();
        while (isRelationalOp())
        {
            Operator op = new Operator(match(token.type()));
            Expression term2 = addition();
            exp = new Binary(op, exp, term2);
        }
        return exp;  // student exercise
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
        //value에 맞는거 매핑해서 리턴하기. int float char true false
        Value value = null;
        String str = token.value();
        if (token.type().equals(TokenType.IntLiteral))
        {
            value = new IntValue(Integer.parseInt(str));
            token = lexer.next();
        }
        else if (token.type().equals(TokenType.FloatLiteral))
        {
            value = new FloatValue(Float.parseFloat(str));
            token = lexer.next();
        }
        else if (token.type().equals(TokenType.CharLiteral))
        {
            value = new CharValue(str.charAt(0));
            token = lexer.next();
        }
        else if (token.type().equals(TokenType.True))
        {
            value = new BoolValue(true);
            token = lexer.next();
        }
        else if (token.type().equals(TokenType.False))
        {
            value = new BoolValue(false);
            token = lexer.next();
        }
        else error("Illegal literal value");
        return value;  // student exercise
    }

    //isbooleanop 없어서 추가
    private boolean isBooleanOp()
    {
        return token.type().equals(TokenType.And) || 
                token.type().equals(TokenType.Or);
    }
    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    //isComma, isSemicolon, isLeftBrace, isRightBrace, isStatement 추가
    private boolean isComma()
    {
        return token.type().equals(TokenType.Comma);
    }

    private boolean isSemicolon()
    {
        return token.type().equals(TokenType.Semicolon);
    }

    private boolean isLeftBrace()
    {
        return token.type().equals(TokenType.LeftBrace);
    }

    private boolean isRightBrace()
    {
        return token.type().equals(TokenType.RightBrace);
    }

    private boolean isIf()
    {
        return token.type().equals(TokenType.If);
    }

    private boolean isWhile()
    {
        return token.type().equals(TokenType.While);
    }

    private boolean isId()
    {
        return token.type().equals(TokenType.Identifier);
    }

    private boolean isStatement()
    {
        return isSemicolon() || isLeftBrace() || isIf() || isWhile() || isId();
    }
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display(0);           // display abstract syntax tree
    } //main

} // Parser
