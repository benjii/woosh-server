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
		<p>The Woosh API is a RESTful interface for manipulating cards, card data, offers, and scans.</p>
		<p><b><h2>Cards</h2></b></p>
		<p><b><h3>Create A New Card</h3></b></p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>URL:</b>&nbsp;http://&lt;server_endpoint&gt;/woosh/card</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Method:</b>&nbsp;POST</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Parameters:</b>&nbsp;name</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Response:</b>&nbsp;200 (OK)</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Example:</b>&nbsp;http://&lt;server_endpoint&gt;/woosh/card&amp;name=Foo</p>
		<p><b><h3>List All Cards For A User</h3></b></p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>URL:</b>&nbsp;http://&lt;server_endpoint&gt;/woosh/cards</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Method:</b>&nbsp;GET</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Parameters:</b>&nbsp;none</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Response:</b>&nbsp;200 (OK)</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Example:</b>&nbsp;http://&lt;server_endpoint&gt;/woosh/cards</p>
		<p><b><h3>Get Card By ID</h3></b></p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>URL:</b>&nbsp;http://&lt;server_endpoint&gt;/woosh/card/{id}</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Method:</b>&nbsp;GET</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Parameters:</b>&nbsp;id</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Response:</b>&nbsp;200 (OK) if found, 404 (NOT_FOUND) otherwise</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Example:</b>&nbsp;http://&lt;server_endpoint&gt;/woosh/card/123456</p>
		<p><b><h2>Card Data</h2></b></p>
		<p><i>Note that there are no card data list methods - card data is provided on the listing methods for cards.</i></p>
		<p><b><h3>Adding Data To A Card</h3></b></p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>URL:</b>&nbsp;http://&lt;server_endpoint&gt;/woosh/card/data</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Method:</b>&nbsp;POST</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Parameters:</b>&nbsp;card-id, name, value, type [BIN,TXT]</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Response:</b>&nbsp;200 (OK) if data added to card, 404 (NOT_FOUND) is card ID not found</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;<b>Example:</b>&nbsp;http://&lt;server_endpoint&gt;/woosh/card/data/cardId=123456&name=Foo&value=Bar&type=TXT</p>
		<p><b><h2>Offers</h2></b></p>
		<p><b><h3>Create A New Offer</h3></b></p>
	</div>
		
</body>

</html>
