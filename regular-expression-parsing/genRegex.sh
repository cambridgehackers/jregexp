#!/bin/bash

javac JLex/REParser.java
java JLex.REParser C . ../patterns/ftp.category.pat jregexp jregexp
