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
				<#assign formNodeValidation = mapOfFormNodeValidation[model.node.id]>
				<div class="variables">
					<#assign formNodeValidationGetFieldConfigs = formNodeValidation.getFieldConfigs()>
					<table class="data">
						<tr>
							<th>Переменная</th>
							<th>Проверка ввода</th>
						</tr>
						<#list formNodeValidationGetFieldConfigs?keys as variableName>
							<tr>
								<td>${variableName}</td>
								<td>
									<#list formNodeValidationGetFieldConfigs[variableName]?keys as nodeFieldConfigsValueKey> 
										<ul>
											<li>Тип проверки: ${validatorDefinitions[nodeFieldConfigsValueKey].getLabel()}</li>
											<li>
												Описание проверки: 
												<#if formNodeValidationGetFieldConfigs[variableName][nodeFieldConfigsValueKey].getDescription()?length == 0>
													${validatorDefinitions[nodeFieldConfigsValueKey].getDescription()} (default)
												<#else>
													${formNodeValidationGetFieldConfigs[variableName][nodeFieldConfigsValueKey].getDescription()}
												</#if>
											</li>
											<#if formNodeValidationGetFieldConfigs[variableName][nodeFieldConfigsValueKey].getMessage()?length != 0>
												<li>Сообщение проверки: ${formNodeValidationGetFieldConfigs[variableName][nodeFieldConfigsValueKey].getMessage()}</li>
											</#if>
											<#if formNodeValidationGetFieldConfigs[variableName][nodeFieldConfigsValueKey].getParams()?size != 0>
												<li>Параметры проверки:</li> 
												<ul>
													<#list formNodeValidationGetFieldConfigs[variableName][nodeFieldConfigsValueKey].getParams()?keys as parameterName>
														<li>${parameterName}: ${formNodeValidationGetFieldConfigs[variableName][nodeFieldConfigsValueKey].getParams()[parameterName]}</li>
													</#list>
												</ul>
											</#if>
										</ul>
									</#list>
								</td>
							</tr>
						</#list>
						<#if globalValidatorDefinitionsMap[model.node.getId()]?size != 0>
							<tr>
								<td><span class="name">Комплексные проверки данных</span></td>
								<td>
									<ul>
										<#list globalValidatorDefinitionsMap[model.node.getId()] as validatorConfig>
											<li>
												<#if validatorConfig.getDescription()?length != 0>
													Описание: ${validatorConfig.getDescription()}
												</#if>
												<#if validatorConfig.getMessage()?length != 0>
													Сообщение валидатора: ${validatorConfig.getMessage()}
												</#if>
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