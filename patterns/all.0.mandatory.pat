# ALL.0
# SHELLCODE unescape encoded shellcode 1
unescape.*(spray|return_address|payloadcode|shellcode|retaddr|retaddress|block|payload|agent|hspt).*unescape[\x20\x09\x0a\x0b\x0d]*\x28(\x22|\x27|\x26quot\x3B|\x5c\x22)[\x25\x5c]u[0-9a-f][0-9a-f][0-9a-f][0-9a-f](\x22[\x20\x09\x0a\x0b\x0d]*\x2B[\x20\x09\x0a\x0b\x0d]*\x22)?[\x25\x5c]u[0-9a-f][0-9a-f][0-9a-f][0-9a-f]
