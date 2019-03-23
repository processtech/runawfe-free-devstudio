<div class="title"><strong>Регламент выполнения бизнес-процесса ${processName}</strong></div>

<#if processDescription ?? || processHtmlDescription ??>
	<div><strong>Общее описание бизнес-процесса</strong></div>
</#if>

<#if processDescription ?? >
    <div><strong>Краткое описание бизнес-процесса:</strong>
    ${processDescription} </div>
</#if>

<#if processHtmlDescription ?? >
    <div><strong>Подробное описание бизнес-процесса:</strong> 
    ${processHtmlDescription}</div>
</#if>

<div class="swimlanes">
	<strong>Список ролей бизнес-процесса:</strong>
	<#list swimlanes as swimlane>
		<div class="swimlanesNames">${swimlane.getName()}</div>
	</#list>
</div>
<div class="variables">
	<strong>Список переменных бизнес-процесса:</strong>
	<#list variables as variable>
		<div class="variablesNames">${variable.getName()}</div>
	</#list>
	
</div>
<div class="nodes">
	<strong>Описание действий бизнес-процесса:</strong>
	<#list nodeModels as model>
		<div class="node ${model.node.typeDefinition.bpmnElementName}">
			<#if model.properties.description?length != 0>
				<div class="description">${model.properties.description}</div>
			</#if>
		</div>
	</#list>
</div>