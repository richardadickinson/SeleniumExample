opt("WinSearchChildren",1)

$Title = $CmdLine[1]
$Username = $CmdLine[2]
$Password = $CmdLine[3]

While 1
	WinActivate("[CLASS:Chrome_WidgetWin_0]", "")
	if WinExists("[CLASS:ViewsTextfieldEdit; INSTANCE:2]") Then
		ControlClick("about:blank - Google Chrome","","[CLASS:ViewsTextfieldEdit; INSTANCE:2]")
		Send($Username,1)
		Send("{TAB}")
		Send($Password,1)
		Send("{ENTER}")
		Exit
	EndIf
	Sleep(1000)
WEnd