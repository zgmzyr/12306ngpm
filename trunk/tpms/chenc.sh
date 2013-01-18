#!/bin/bash
for file in *.java; do
    iconv -f cp936 -t utf-8 "$file" -o "$file"
done
