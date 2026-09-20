Set fso = CreateObject("Scripting.FileSystemObject")
scriptDir = fso.GetParentFolderName(WScript.ScriptFullName)
batPath = scriptDir & "\START_BACKEND.bat"
Set WshShell = CreateObject("WScript.Shell")
WshShell.Run Chr(34) & batPath & Chr(34), 0, False
Set WshShell = Nothing
