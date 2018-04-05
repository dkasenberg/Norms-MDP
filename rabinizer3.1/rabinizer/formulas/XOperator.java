package rabinizer.formulas;

public class XOperator extends FormulaUnary {

    @Override
    public String operator() {
        return "X";
    }

    public XOperator(Formula f) {
        super(f);
    }

    @Override
    public XOperator ThisTypeUnary(Formula operand) {
        return new XOperator(operand);
    }
    
    public Formula applyParam(String k, String v) {
        return new XOperator(operand.applyParam(k, v));
    }

    @Override
    public Formula unfold() {
        return this;
    }
    
    public Formula copy() {
        return new XOperator(operand.copy());
    }

    @Override
    public Formula unfoldNoG() {
        return this;
    }

    @Override
    public Formula toNNF() {
        return new XOperator(operand.toNNF());
    }

    @Override
    public Formula negationToNNF() {
        return new XOperator(operand.negationToNNF());
    }

    //============== OVERRIDE ====================
    @Override
    public Formula removeX() {
        return operand;
    }

}
