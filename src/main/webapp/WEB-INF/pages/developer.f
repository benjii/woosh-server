<!DOCTYPE html>

<html>

<head>

	<style>
		#header {
		  	width: 400px ;
		  	margin-left: auto ;
		 	margin-right: auto ;
		 	margin-top: 75px ;
			text-align:center;
			font-size:20pt;
			font-family:helvetica;
		}

		#documentation {
		  	margin-left: 50px;
		 	margin-top: 75px ;
			font-size:11pt;
			font-family:helvetica;
		}

	</style>

</head>

<title>Woosh</title>

<body>
	<div id='header'>Woosh API Documentation</div>

	<div id='documentation'>
		<p>The Woosh API works by utilising a single endpoint (/m/data), through which JSON documents are passed. Those JSON documents represent
		cards, offers, acceptances, and scans.</p>
		<p>Listed below are the specific JSON formats for each of those entity types.</p>
		<p>
		<p><b><h3>Cards</h3></b></p>
		<p>
		<p><b><h3>Offers</h3></b></p>
		<p>
		<p><b><h3>Acceptances</h3></b></p>
		<p>
		<p><b><h3>Scans</h3></b></p>		
		<p>A scan, sometimes referred to as a 'woosh up', allows a user to scan for available offers in their local area. The rules for what
		constitutes an available offer can be complex, but in it's simplest form an offer is available if;
		<ol>
			<li>The user is within the geographic catchment area of the offer;</li>
			<li>The scan takes place at a time when the offer is available;</li>
			<li>The offer has not reached it's maximum number of acceptances.</li>
		</ol>
		<p><b><i>Sample JSON</i></b></p>
		<pre>
		&quot;scans&quot; : [
    		&nbsp;&nbsp;&nbsp;&nbsp;{
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;scannedAt&quot; : &quot;2012-12-02T12:21:45.203GMT&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;deleted&quot; : &quot;false&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;clientId&quot; : &quot;95D83D70-B2E4-4F83-BF9D-FA9BEE433A59&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;lastUpdated&quot; : &quot;2012-12-02T12:21:45.202GMT&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;location&quot; : &quot;POINT(-122.406417 0.000000)&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;clientVersion&quot; : &quot;-1&quot;
    		&nbsp;&nbsp;&nbsp;&nbsp;}
		</pre>
	</div>
	
</body>

</html>
