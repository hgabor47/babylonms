git add .
git commit -m %1
git push origin master
rd "..\temp" 
md "..\temp"
xcopy "main\c#" "..\temp" /E /C /Y
git checkout c#
xcopy "..\temp" "main\c#" /E /C /Y
git add .
git commit -m %1
git push origin c#
git checkout master

