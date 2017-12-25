<div class="nodes">
	<#list nodeModels as model>
		<div id="${model.node.id}" class="node ${model.node.typeDefinition.bpmnElementName}">
			<div class="header">
				<span class="step">Шаг ${model?counter}.</span>
				<span class="type">${model.node.typeDefinition.label}</span>
				<span class="name">${model.node.name}</span>
			</div>
			<#if model.inEmbeddedSubprocess>
				<div class="embedded-subprocess">Действие в рамках композиции <span class="name">${model.node.processDefinition.name}</span></div>
			</#if>
			<#if model.swimlane ?? >
				<div class="swimlane">Роль: <span class="name">${model.swimlane.name}</span></div>
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
	</#list>
</div>
<#if swimlaneModelEnable >
	<div class="swimlanes">	
		<br />
		<div class="header">Список ролей:</div>
		<#list swimlaneModels as model>
			<div id="${model.id}" class="node">
				<div class="header">
					<span class="name">${model.name}</span>
				</div>
			</div>
		</#list>
	</div>
</#if>
<#if variableModelEnable >
	<div class="variables">
		<br />
		<div class="header">Список переменных:</div>
		<#list variableModels as model>
			<div id="${model.id}" class="node">
				<div class="header">
					<span class="name">${model.name}</span>
				</div>
			</div>
		</#list>
	</div>
</#if>