{
	"schema": {
<#list schema?keys as alias>
		"${alias}": [
			{
<#list schema[alias].fields as f>
				"${f.name}": "${f.type}"<#if f_has_next>,</#if>
</#list>
			}
		]<#if alias_has_next>,</#if>
</#list>
	}
}