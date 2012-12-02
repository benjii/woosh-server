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
		<p>The Woosh API works by utilising a single endpoint through which JSON documents are sent and received. Those JSON
		documents represent cards, offers, acceptances, and scans.</p>
		<p><b><h2>A Single Endpoint</h2></b></p>
		<p>To send data to the Woosh servers the following URL is used (always specifying the HTTP POST method);</p>
		<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>http://&lt;server address&gt;/woosh/m/data?v=1.0&amp;p=0&amp;ts=&lt;last client update&gt;</i></p>
		Note: Woosh understands UTC-format timestamps in all cases (for example, 2012-12-02T18:15:03.897GMT). This is true for the 'ts' parameter above.
		<p>
		The data payload is expressed as a JSON document, which is always of the general form;</p>
		<pre>
		{
  			&quot;offers&quot; : [
  				{
  					&lt;offer entities&gt;
  				}
  			],
  			&quot;acceptances&quot; : [
  				{
  					&lt;acceptance entities&gt;
  				}
  			],
  			&quot;scans&quot; : [
    			{
  					&lt;scan entities&gt;
    			}
  			],
  			&quot;cards&quot; : [
  				{
  					&lt;card entities&gt;
  				}
  			],
  			&quot;carddata&quot; : [
  				{
  					&lt;card data entities&gt;
  				}
  			]
		}
		</pre>
		<p>The Woosh servers will process incoming JSON payloads and take appropriate action. That is, create / update / remove entities. You
		only need to send the sections that are relevant (i.e.: if you are creating a new card you can omit the acceptance, offer, and scan
		sections entirely). For the specific properties of each entity type, see below.</p>
		<p>
		<p><b><h2>Entity Types &amp; Definitions</h2></b></p>
		<p>A general note about entities: all entity types have the 'deleted', 'clientId', 'lastUpdated', and 'clientVersion' properties. These
		are used so that the Woosh servers can correctly handle entity creation, update, and deletion.</p>
		<p>
		<ol>
			<li><b><i>deleted:</i></b> true or false;</li>
			<li><b><i>clientId:</i></b> a globally unique client-generated ID (recommend use of time-based UUID);</li>
			<li><b><i>lastUpdated:</i></b> the time at which the entity was last updated (allows for server-side conflict resolution);</li>
			<li><b><i>clientVersion:</i></b> the version of the entity (-1 if it is new, zero or greather otherwise) - increment this value on every update;</li>
		</ol>
		</p>
		<p><b><h3>Cards</h3></b></p>
		<p>Woosh cards are what users offer and accept between each other. Cards hold card data or various types (e.g.: text, photos, and audio).</p>
		<p>When a card is accepted by a user it is, by default, cloned (that is, the accepting user gets their own copy). These accepted cards
		will then be sent to the users device(s) by the Woosh servers.</p>
		<p><b><i>Sample JSON</i></b></p>
		<pre>
		&quot;cards&quot; : [
    		&nbsp;&nbsp;&nbsp;&nbsp;{
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;name&quot; : &quot;Message from ben.deany&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;deleted&quot; : &quot;false&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;clientId&quot; : &quot;62B7598D-43FF-4A05-AD35-AEDD8BFF3AAA&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;lastUpdated&quot; : &quot;2012-12-02T18:42:16.883GMT&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;clientVersion&quot; : &quot;-1&quot;
    		&nbsp;&nbsp;&nbsp;&nbsp;}
    		&nbsp;&nbsp;]
		</pre>
		<p>And the related card data (one card can have any number of card data items);</p>
		<pre>
		&quot;carddata&quot; : [
    		&nbsp;&nbsp;&nbsp;&nbsp;{
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;name&quot; : &quot;default&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;data&quot; : &quot;Hello, world!&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;card&quot; : &quot;62B7598D-43FF-4A05-AD35-AEDD8BFF3AAA&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;deleted&quot; : &quot;false&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;clientId&quot; : &quot;5A396E50-2937-4BD7-A166-7A5514E3C86F&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;lastUpdated&quot; : &quot;2012-12-02T18:42:16.883GMT&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;clientVersion&quot; : &quot;-1&quot;
    		&nbsp;&nbsp;&nbsp;&nbsp;}
    		&nbsp;&nbsp;]
		</pre>
		<p><b><h3>Offers</h3></b></p>
		<p>Offers can be made by any user with at least one Woosh card. Offers have a start and end (expiry) time, as well a region within
		which they are valid. The offer region is expressed as a GIS Well-Known Text (WKT) format string (currently only POINT is supported). 
		<pre>
		&quot;scans&quot; : [
    		&nbsp;&nbsp;&nbsp;&nbsp;{
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;offerStart&quot; : &quot;2012-12-02T18:52:25.161GMT&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;offerEnd&quot; : &quot;2012-12-02T18:57:25.161GMT&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;offerRegion&quot; : &quot;POINT(-122.406417 0.261763)&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;card&quot; : &quot;62B7598D-43FF-4A05-AD35-AEDD8BFF3AAA&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;deleted&quot; : &quot;false&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;clientId&quot; : &quot;95D83D70-B2E4-4F83-BF9D-FA9BEE433A59&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;lastUpdated&quot; : &quot;2012-12-02T12:21:45.202GMT&quot;,
      			&nbsp;&nbsp;&nbsp;&nbsp;&quot;clientVersion&quot; : &quot;-1&quot;
    		&nbsp;&nbsp;&nbsp;&nbsp;}
		</pre>
		<p>
		<p><b><h3>Acceptances</h3></b></p>
		All offers in Woosh are currently set to be auto-accepted. Therefore, there is never (currently) a necessity to send an acceptance
		of an offer from the client devices to the Woosh servers.
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
		<p><b><h2>Receiving Data</h2></b></p>
		<p>To receive data from the Woosh servers (for example, to get any new cards that the user has accepted) simply call the standard
		URL with no payload. If there is anything new for the user to receive, it will be expressed as JSON and send back to the client.
		The general form of this JSON is;</p>
		<pre>
		{
			&quot;pageNum&quot;: &quot;0&quot;,
			&quot;remainingPages&quot;: &quot;0&quot;,
			&quot;updateTime&quot;: &quot;2012-12-02T18:52:25.401GMT&quot;,
			&quot;entities&quot;: {
				&lt;any entities for the user - expressed in the form as above&gt;
			},
			&quot;receipts&quot;: {
			&quot;offers&quot;: [
				{
					"id": "DCDA10E9-7750-419E-9E5A-A55BE9D009EE",
					"version": "0",
					"lastUpdated": "2012-12-02T18:52:25.373+0000"
				}
			]
		}
		</pre>
		<p><b><h2>Receipts</h2></b></p>
		<p>Note that in the previous section we saw an example of a Woosh server payload - this response includes receipt data. For every
		entity create, update, and remove operation sent to the Woosh servers, a receipt will be sent back acknowledging delivery and
		successful processing of the entities.</p>
		<p>It is therefore possible for client devices to check delivery and guarantee correct transmission of all data to the Woosh servers
		(and to perform actions such as retrying it required, etc).</p>
	</div>
	
</body>

</html>
