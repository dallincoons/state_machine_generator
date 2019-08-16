package smc.generators.nestedSwitchCaseGenerator;

public interface NSCNodeVisitor {
    public void visit(NSCNode.SwitchCaseNode switchCaseNode);
    public void visit(NSCNode.CaseNode caseNode);
    public void visit(NSCNode.FSMClassNode fsmClassNode);
    public void visit(NSCNode.EnumNode enumNode);
    public void visit(NSCNode.StatePropertyNode statePropertyNode);
    public void visit(NSCNode.EventDelegatorsNode eventDelegatorsNode);
    public void visit(NSCNode.HandleEventNode handleEventNode);
    public void visit(NSCNode.FunctionCallNode functionCallNode);
    public void visit(NSCNode.EnumeratorNode enumeratorNode);
    public void visit(NSCNode.DefaultCaseNode defaultCaseNode);
}
