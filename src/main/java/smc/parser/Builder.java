package smc.parser;

public interface Builder {
    void headerError(ParserState state, ParserEvent event, int line, int pos);
    void stateError(ParserState state, ParserEvent event, int line, int pos);
    void transitionError(ParserState state, ParserEvent event, int line, int pos);
    void transitionGroupError(ParserState state, ParserEvent event, int line, int pos);
    void endError(ParserState state, ParserEvent event, int line, int pos);
    void syntaxError(int line, int pos);
    void newHeaderWithName();
    void addHeaderWithValue();
    void done();
    void setName(String name);
    void setStateName();
    void setEvent();
    void setNullEvent();
    void setEntryAction();
    void setExitAction();
    void setNextState();
    void setNullNextState();
    void transitionWithAction();
    void transitionNullAction();
    void addAction();
    void transitionWithActions();
    void setSuperStateName();
    void setStateBase();
}
