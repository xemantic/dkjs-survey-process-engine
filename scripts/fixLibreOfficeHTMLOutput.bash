#!/usr/bin/bash

# Tool to clean up .doc file saved as HTML by Libre Office

# Dependency: sudo apt install tidy

# Usage:
# 1. Open the .doc file send by DKJS with Libre Office
# 2. Save it as Mailtexte.AUF.leben.html
# 3. Run this script to clean it up

cp Mailtexte.AUF.leben.html tmp.html

sed -i \
  -e 's/<\/font>//g'              `# remove </font>` \
  -e 's/ style="[^"]*"//g'        `# remove style=".*"` \
  -e 's/ align="[^"]*"//g'        `# remove align=".*"` \
  -e 's/<font[^>]*>//g'           `# remove <font.*>` \
  -e 's/<span>//g'                `# remove <span>` \
  -e 's/<\/span>//g'              `# remove </span>` \
  -e 's/<meta [^>]*>//g'          `# remove <meta.*>` \
  -e 's/ name="[^"]*"//g'         `# remove name=".*"` \
  -e 's/<br\/>//g'                `# remove <br/>` \
  -e 's/&nbsp;/ /g'               `# remove &nbsp` \
  -e 's/<li><p>/<li>/g'           `# remove <p> in <li>` \
  -e 's/<\/u><\/a>/<\/a>/g'       `# remove </u> before </a>` \
  -e 's/"><u>/">/g'               `# remove <u> behind tag` \
  -e 's/<\/\(\w\)>/<\/\1> /g'     `# add spaces after closing tags` \
  -e 's/<\(\w\)>/ <\1>/g'         `# add spaces before opening tags` \
  -e '/<style type/,/<\/style>/d' `# delete style block` \
  tmp.html

tidy -omit -o Mailtexte.AUF.leben.clean.html tmp.html

echo "Next:"
echo "- Open Mailtexte.AUF.leben.clean.html in Idea"
echo "- Clean possible errors manually"
echo "- Chop and paste into corresponding body.html files"
echo "Notice SECTION_NAME, subject, header, body, and footer"

