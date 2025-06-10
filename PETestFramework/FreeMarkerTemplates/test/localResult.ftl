<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>PM Test Result</title>
		<link rel="stylesheet" type="text/css" href="${cssPath}">
		<script language="JavaScript">
						function toggleDiv(divname) {
							var ele = document.getElementById(divname);
							if (ele.style.display == "none") {
								ele.style.display = "block";
							}
							else {
								ele.style.display = "none";
							}
						}  
          </script>
	</head>
	<#macro getTestColour result><#switch result>
								<#case "SUCCESS">w3-green<#break>
								<#case "FAILCOMPARE">w3-red<#break>
								<#case "FAILJES">w3-red<#break>
								<#case "unexpectedPass">w3-amber<#break>
								<#default>w3-light-grey</#switch></#macro> 
   	<body>
		<div class="w3-card-4" id="RunDetails" style="display: block;">
			<header class="w3-container <@getTestColour result/>">
				<h2>Test ${testName} from spec ${specName}</h2>
			</header>
			<div class="w3-container">
				<h4>Date: ${.now}</h4>
			</div>
			<footer class="w3-container <@getTestColour result/>">
				<h3>Result ${result}</h3>
			</footer>
			<div class="w3-card-4" id="RunDetails" style="display: block;">
				<header class="w3-container <@getTestColour result/>">
					<h3>Run Control Analyser Ouput</h3>
				</header>
				<div class="w3-card-4">
				<a href="rca/gvbrca.html">See Analysis here</a>
				</div>
			</div>
		</div>		
   </body>
</html>