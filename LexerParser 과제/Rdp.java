public class Rdp {
    public static void main(String args[])
    {
        Parser parser = new Parser(new Lexer("p2.cl"));
        Program prog = parser.program();
        prog.display(0);
    }
}