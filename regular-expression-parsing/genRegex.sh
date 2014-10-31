#!/bin/bash

javac JLex/REParser.java
java JLex.REParser bin . ../patterns/ftp.category.pat jregexp jregexp 32 32
