# Bittorrent - P2P filesharing / publishing tool - http://www.bittorrent.com
# Pattern attributes: good slow notsofast undermatch
# Protocol groups: p2p open_source
# Wiki: http://www.protocolinfo.org/wiki/Bittorrent
# Copyright (C) 2008 Matthew Strait, Ethan Sommer; See ../LICENSE
#
# This pattern has been tested and is believed to work well.
# It will, however, not work on bittorrent streams that are encrypted, since
# it's impossible to match (well) encrypted data.

# Does not attempt to match the HTTP download of the tracker
# 0x13 is the length of "bittorrent protocol"
# Second two bits match UDP wierdness
# Next bit matches something Azureus does
# Ditto on the next bit.  Could also match on "user-agent: azureus", but that's in the next
# packet and perhaps this will match multiple clients.
# bitcomet-specific strings contributed by liangjun.

# This is not a valid GNU basic regular expression (but that's ok).

# BITTORRENT.CATEGORY
(\x13bittorrent\x20protocol|azver\x01|get\x20/scrape\?info_hash=get\x20/announce\?info_hash=|get\x20/client/bitcomet/|GET\x20/data\?fid=)|d1:ad2:id20:|\x08'7P\)[RP]
