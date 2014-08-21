
// This is an example Verilog DFA.


// There seems to be no good way in Verilog to pass the DFA functions
// in as parameters. So instead we just generate this entire module
// around the DFA. See the Verilog/ directory for examples.

// Note: If you change this code be sure to update REParser.java to match.

module RegexMatcher(clk, 
                    rst_n, 
                    char_in, 
                    char_in_vld, 
                    state_in, 
                    state_in_vld, 
                    state_out, 
                    accept_out);

    // The clock and reset info.
    input clk, rst_n;
    // Input character, and state, if being set.
    input [7:0] char_in, state_in;
    // char_in_vld should be true if there's a character to process.
    // state_in_vld should be true if the outside world is overwriting our state.
    input char_in_vld, state_in_vld;
    // state_out is our current state.
    output [7:0] state_out;
    // Accept out is true if the character triggered a regex match.
    output accept_out;

    // A register for the current state.
    reg [7:0] cur_state;

    // DFA DEFINITION GOES HERE
    

    // Invoke the DFA functions.    
    wire [7:0] mapped_char, mapped_state;
    wire [7:0] next_state;
    wire next_accept;

    assign mapped_char = charMap(char_in);
    assign mapped_state = stateMap(cur_state);
    assign next_state = stateTransition(mapped_state, mapped_char);
    assign next_accept = acceptStates(next_state);

    // Update our outputs.
    assign accept_out = state_in_vld ? 1'b0 : char_in_vld ? next_accept : 1'b0;
    assign state_out = cur_state;
    
    // Update our local state.
    always @(posedge clk)
    begin
       if (!rst_n)
        begin
            cur_state <= 0;
        end
        else
        begin
            if (state_in_vld)
            begin
                cur_state <= state_in;
            end
            else if (char_in_vld)
            begin
                cur_state <= next_state;
            end
        end
    end

endmodule
