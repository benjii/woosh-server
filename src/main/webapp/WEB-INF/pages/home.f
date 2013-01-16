<!DOCTYPE html>

<html>

<head>

	<style>
		#header {
		  	width: 400px ;
		  	margin-left: auto ;
		 	margin-right: auto ;
		 	margin-top: 15px ;
			text-align:center;
			font-size:20pt;
			font-family:helvetica;
		}

		#copy {
		  	width: 600px ;
		  	margin-left: auto ;
		 	margin-right: auto ;
		 	margin-top: 35px ;
			text-align:center;
			font-size:11pt;
			font-family:helvetica;
		}

		#gfx {
		  	width: 800px ;
		  	margin-left: auto ;
		 	margin-right: auto ;
		 	margin-top: 50px ;
			text-align:center;
			font-size:9pt;
			font-family:helvetica;
		}

	</style>

</head>

<title>Woosh</title>

<body>
	<div id='header'>Woosh Server</div>

	<div id='copy'>
		<p>Users have created ${full_card_count} cards, of which ${card_count} are currently active (non-deleted).</p>
		<p>Collectively, those cards have been offered ${offer_count} times.</p>
		<p>Those offers have been accepted ${acceptance_count} times.</p>
		<p>Overall, users have performed ${scan_count} scans for offers.</p>
	</div>

	<div id='gfx'>
		<img src="/woosh/resources/demo01_30fps_64colours.gif" alt="Woosh" width="480" height="360">
	</div>

	<div id='copy'>
		<p>Developer? Click <a href="woosh/restapi">here</a> to find out about the API.</i>
	</div>
	
</body>

</html>
