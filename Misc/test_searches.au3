
$path = $CmdLine[1]

WinWaitActive("[CLASS:#32770]", "",10)
ControlClick("[Class:#32770]","","[CLASS:ComboBox; INSTANCE:1]")
Send($path,1)
Send("{ENTER}")
Exit
	