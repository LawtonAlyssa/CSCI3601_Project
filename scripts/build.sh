echo Building project
if mvn compile; then
    clear
else
    exit 1
fi