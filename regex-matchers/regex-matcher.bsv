import SystemTypes::*;

typedef Bit#(8) Char;
typedef Bit#(8) RegexState;

interface REGEX_MATCHER;

  method ActionValue#(RegexState) swapState ( RegexState newState );
  method Action processChar ( Char in );
  method Bool isMatched ();
  method Action clearMatch ();
  method RegexState getState ();
  method Action setState (RegexState newState);

endinterface

// This is the regular expression matcher module.
// The charMap function converts chars to a smaller input set. We still use Char type for mappedIn but only the first few bits are used. The 
// compiler will optimize the excess bits out of the state transition function. (Without the charMap, the table would have to consider 256 inputs).

module mkRegexMatcherReal#( function Char charMap(Char in),
                            function RegexState stateMap(RegexState state),
                            function RegexState stateTransition( RegexState state, Char mappedIn ),
                            function Bool acceptStates(RegexState state),
                            String regexName)
                      ( REGEX_MATCHER );

  Reg#(RegexState)  state <- mkReg(0);
  Reg#(Bool)        regexMatched <- mkReg(False);
  Reg#(File)        debugLog <- mkReg(InvalidFile);
  
  rule openFile (debugLog == InvalidFile);
  
    let fhandle <- $fopen("regex_matcher_" + regexName, "w");
    
    if (fhandle == InvalidFile)
    begin
    
        String errStr = "ERROR: Unable to open logfile: regex_matcher_" + regexName;
        $display(errStr);
        $finish(1);
    
    end

    debugLog <= fhandle;
  
  endrule

  method ActionValue#(RegexState) swapState ( RegexState newState );
    state <= newState;
    return state;
  endmethod

  method Action processChar ( Char in ) if (!regexMatched);
    if ( state[7] == 0 )  //hack
    begin
      Char mappedIn = charMap( in );
      RegexState mappedState = stateMap( state );
      RegexState nextState = stateTransition( mappedState, mappedIn );
      $fdisplay(debugLog, "State: %d, MappedState: %d, In: %s (0x%x), MappedIn: %d, Matched: %d", state, mappedState, in, in, mappedIn, acceptStates(nextState));
      if ( acceptStates(nextState) )
      begin
        $fdisplay(debugLog, "MATCH!");
        regexMatched <= True;
      end
      else
        state <= nextState;
    end
  endmethod

  method Bool isMatched ( );
    return regexMatched;
  endmethod

  method Action clearMatch ( );
    regexMatched <= False;
    if (regexMatched == True)
      state[7] <= 1; //i.e. stop matching
  endmethod

  method RegexState getState ();
    return state;
  endmethod

  method Action setState (RegexState newState);
    state <= newState;
  endmethod

endmodule

// A Fake RegexMatcher will never match anything.
// This can be used to implement disabled patterns.

module mkRegexMatcherFake#( String regexName)
                      ( REGEX_MATCHER );
         
  method ActionValue#(RegexState) swapState ( RegexState newState );
    noAction;
  endmethod

  method Action processChar ( Char in );
    noAction;
  endmethod

  method Bool isMatched ( );
    return False;
  endmethod

  method Action clearMatch ( );
    noAction;
  endmethod

  method RegexState getState ();
    return 0;
  endmethod

  method Action setState (RegexState newState);
    noAction;
  endmethod
 
endmodule

