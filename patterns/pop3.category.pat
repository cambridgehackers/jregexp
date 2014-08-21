# POP3 - Post Office Protocol version 3 (popular e-mail protocol) - RFC 1939
# Pattern attributes: great fast fast
# Protocol groups: mail ietf_internet_standard
# Wiki: http://www.protocolinfo.org/wiki/POP
# Copyright (C) 2008 Matthew Strait, Ethan Sommer; See ../LICENSE
#
# This pattern has been tested somewhat.

# this is a difficult protocol to match because of the relative lack of 
# distinguishing information.  Read on.
#pop3

# Here's another tack. I think this is my second favorite.
(\+[Oo][Kk]\x20[\x09-\x0d\x20-~]*([Rr][Ee][Aa][Dd][Yy]|[Hh][Ee][Ll][Ll][Oo]|[Pp][Oo][Pp]|[Ss][Tt][Aa][Rr][Tt][Ii][Nn][Gg])|-[Ee][Rr][Rr]\x20[\x09-\x0d\x20-~]*([Ii][Nn][Vv][Aa][Ll][Ii][Dd]|[Uu][Nn][Kk][Nn][Oo][Ww][Nn]|[Uu][Nn][Ii][Mm][Pp][Ll][Ee][Mm][Ee][Nn][Tt][Ee][Dd]|[Uu][Nn][Rr][Ee][Cc][Oo][Gg][Nn][Ii][Zz][Ee][Dd]|[Cc][Oo][Mm][Mm][Aa][Nn][Dd]))

# some sample servers:
# RFC example:        +OK POP3 server ready <1896.697170952@dbc.mtview.ca.us>
# mail.dreamhost.com: +OK Hello there.
# pop.carleton.edu:   +OK POP3D(*) Server PMDFV6.2.2 at Fri, 12 Sep 2003 19:28:10 -0500 (CDT) (APOP disabled)
# mail.earthlink.net: +OK NGPopper vEL_4_38 at earthlink.net ready <25509.1063412951@falcon>
# *.email.umn.edu:    +OK Cubic Circle's v1.22 1998/04/11 POP3 ready <7d1e0000da67623f@aquamarine.tc.umn.edu>
# mail.yale.edu:      +OK POP3 pantheon-po01 v2002.81 server ready
# mail.gustavus.edu:  +OK POP3 solen v2001.78 server ready
# mail.reed.edu:      +OK POP3 letra.reed.edu v2002.81 server ready
# mail.bowdoin.edu:   +OK mail.bowdoin.edu POP3 service (iPlanet Messaging Server 5.2 HotFix 1.15 (built Apr 28 2003))
# pop.colby.edu:      +OK Qpopper (version 4.0.5) at basalt starting.
# mail.mac.com:       +OK Netscape Messaging Multiplexor ready

# various error strings:
#-ERR Invalid command.
#-ERR invalid command
#-ERR unimplemented
#-ERR Invalid command, try one of: USER name, PASS string, QUIT
#-ERR Unknown AUTHORIZATION state command
#-ERR Unrecognized command
#-ERR Unknown command: "sadf'".
