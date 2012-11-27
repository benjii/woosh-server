{
	"pageNum": "${pageNum}",
	"remainingPages": "${remainingPages}",
	"updateTime": "${updateTime?string("yyyy-MM-dd'T'HH:mm:ss.SSSz")}",
	"entities": {
<#list entities?keys as key>
		"${key}": [
<#list entities[key] as e>
			{
<#list e?keys as k>
				"${k}": "${e[k]}"<#if k_has_next>,</#if>
</#list>			
			}<#if e_has_next>,</#if>
</#list>
		]<#if key_has_next>,</#if>
</#list>
	},
	"receipts": {
<#list receipts?keys as rkey>
		"${rkey}": [
<#list receipts[rkey] as r>
			{
				"id": "${r.clientId}",
				"version": "${r.clientVersion?c}",
<#if r.additionalProperties??>
<#list r.additionalProperties?keys as propkey>
				"${propkey}": "${r.additionalProperties[propkey]}",
</#list>
</#if>
				"lastUpdated": "${r.lastUpdated?string("yyyy-MM-dd'T'HH:mm:ss.SSSZ")}"
			}<#if r_has_next>,</#if>
</#list>
		]<#if rkey_has_next>,</#if>
</#list>
	}
}