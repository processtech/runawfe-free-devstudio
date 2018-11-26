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
		<#if model.node.class.simpleName != "EndState" && model.node.class.simpleName != "EndTokenState">
		<div class="node ${model.node.typeDefinition.bpmnElementName}">
			<div class="header">
				<a name="${model.node.id}"></a>
				<span class="step">Шаг:</span>
				<span class="name">${model.node.name}</span>
			</div>
			<#if model.inEmbeddedSubprocess>
				<div class="embedded-subprocess">Действие в рамках композиции <span class="name">${model.node.processDefinition.name}</span></div>
			</#if>
				<div class="type">Тип шага: <span class="name">${model.node.typeDefinition.label}</span></div>
			<#if model.swimlane ?? >
				<div class="swimlane">Роль: <span class="name">${model.swimlane.name}</span></div>
			</#if>
			<#if model.leavingTransitions?size == 1>
				<#list model.leavingTransitions as transition>
					<div class="transition">Далее управление переходит к шагу <span class="name"><a href="#${transition.target.id}">${transition.target.name}</a></span></div>
				</#list>
			<#else>
				<div class="transition">Далее управление переходит к шагу </div>
				<#list model.leavingTransitions as transition>
					<div class="transition">в случае <span class="name">${transition.name}</span><span class="name"><a href="#${transition.target.id}"> ${transition.target.name}</a></span></div>
				</#list>
			</#if>
			<#if model.node.class.simpleName == "SendMessageNode" && model.node.getTtlDuration() ??>
				<div class="ttl">Время жизни сообщения: ${model.node.getTtlDuration().toString()}</div>
			</#if>
			<#if model.node.class.simpleName == "Subprocess" || model.node.class.simpleName == "MultiSubprocess">
				<#if !model.node.embedded>
					<div class="subprocess">Подпроцесс: <span class="name">${model.node.subProcessName}</span></div>
				</#if>
			</#if>
			<#if model.properties.description?length != 0>
				<div class="description">${model.properties.description}</div>
			</#if>
			<#if model.hasFormValidation()>
				<#assign formNodeValidation = model.formNodeValidation>
				<div class="variables">
					<#assign formNodeValidationFieldConfigs = formNodeValidation.getFieldConfigs()>
					<table class="data">
						<tr>
							<th>Переменная</th>
							<th>Проверка ввода</th>
						</tr>
						<#list formNodeValidationFieldConfigs?keys as variableName>
							<tr>
								<td class="variableName">${variableName}</td>
								<td>
									<ul>
										<#list formNodeValidationFieldConfigs[variableName]?values as fieldValidatorConfig> 
											<li>
												<#if fieldValidatorConfig.message?length != 0>
													${fieldValidatorConfig.message}
												<#else>
													${validatorDefinitions[fieldValidatorConfig.type].getDescription()}
												</#if>
												<#if fieldValidatorConfig.transitionNames?size != 0>
													(только в случае <#list fieldValidatorConfig.transitionNames as transitionName>«<b>${transitionName}</b>»<#if transitionName?has_next>, </#if></#list>)
												</#if>
												<#if fieldValidatorConfig.params?size != 0>
													<ul>
														<#list fieldValidatorConfig.params?keys as parameterName>
															<li>
																${validatorDefinitions[fieldValidatorConfig.type].params[parameterName].label}: 
																${model.getLocalized(fieldValidatorConfig.params[parameterName])}
															</li>
														</#list>
													</ul>
												</#if>
											</li>
										</#list>
									</ul>
								</td>
							</tr>
						</#list>
						<#if formNodeValidation.globalConfigs?size != 0>
							<tr>
								<td><span class="name">Комплексные проверки данных</span></td>
								<td>
									<ul>
										<#list formNodeValidation.globalConfigs as globalValidatorConfig>
											<li>
												${globalValidatorConfig.message!"Без сообщения"}
											</li>
										</#list>
									</ul>  
								</td>
							</tr>
						</#if>
					</table>
				</div>
			</#if>
		</div>
		</#if>
	</#list>
</div>
<div class="endings">
	<#list endToken as end>
		<div class="endTokenState">Завершение потока  выполнения бизнес-процесса: ${end.getName()}</div>
	</#list>
	<#list end as end>
		<div class="endState">Завершение процесса выполнения бизнес-процесса: ${end.getName()}</div>
	</#list>
</div>