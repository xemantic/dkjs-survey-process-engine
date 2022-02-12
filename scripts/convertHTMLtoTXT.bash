#!/usr/bin/bash

# Tool to convert html e-mail templates to text templates
# adding header and footer and formatting Thymeleaf variables for text.

cd ../src/main/resources/templates/mail || exit

# Dependency: sudo apt install html2text

# https://www.systutorials.com/docs/linux/man/5-html2textrc/
# https://www.systutorials.com/docs/linux/man/1-html2text/

# Note that this overrides your .html2tetxrc preferences if you ever
# customized this tool in the past!

echo "P.vspace.after=1" > ~/.html2textrc
echo "UL.vspace.after=1" >> ~/.html2textrc

for folder in *
do
  if [ -d "$folder" ];
  then
    from=$folder/body.html
    to=$folder/body.txt
    html2text -utf8 -o "$to" "$from"

    sed -i \
      -e '1i\[( ~{mail/header} )]\n'      `# add header` \
      -e '/./,$!d'                        `# remove leading empty lines` \
      -e 's/[{]\(\w\+\)[}]/[( ${\1} )]/g' `# convert {a} into [( $a )] (thymeleaf)` \
      -e '$a\[( ~{mail/footer} )]'        `# add footer` \
      "$to"
  fi
done

