rd "..\temp"
md "..\temp"
xcopy "main\c#" "..\temp" /E /C /Y
git checkout c#
xcopy "..\temp" "main\c#" /E /C /Y
git add .
git push origin c#
git checkout master

