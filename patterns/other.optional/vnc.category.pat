# VNC - Virtual Network Computing.  Also known as RFB - Remote Frame Buffer
# Pattern attributes: great veryfast fast
# Protocol groups: remote_access
# Wiki: http://www.protocolinfo.org/wiki/VNC
# Copyright (C) 2008 Matthew Strait, Ethan Sommer; See ../LICENSE
#
# http://www.realvnc.com/documentation.html
# 
# This pattern has been verified with vnc v3.3.7 on WinXP and Linux
#
# Thanks to Trevor Paskett <tpaskett AT cymphonix.com> for this pattern.

# vnc
# Assumes single digit major and minor version numbers 

# VNC.CATEGORY
rfb\x2000[1-9]\.00[0-9]\x0a
