opt("WinSearchChildren",1)

$Title = $CmdLine[1]
$Username = $CmdLine[2]
$Password = $CmdLine[3]

While 1
    If WinActive($Title) Then
        ControlClick($Title,"","#32770","Left",1,85,60)
        Send($Username,1)
        Send("{TAB}")
        Send($Password,1)
        Send("{ENTER}")
		Exit
    EndIf
    Sleep(1000)
WEnd
