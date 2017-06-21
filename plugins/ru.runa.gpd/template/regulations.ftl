<table width="70%" border="0" align="center">
	<tr bgcolor="#e3f4ff"><td><strong>Общее описание бизнес-процесса</strong></td></tr>
	<tr><td> 

	<#if proc.getDescription() ?? >
		
	<br/>
	<p><strong>  Краткое описание бизнес-процесса: ${proc.getDescription()} </strong> </p>
	</#if>
	
	<#if brief ?? >
		<p> <strong> Подробное описание бизнес-процесса: </strong> </p>
		${brief}
	</#if>
	
	<#-- SWIMLANES -->
	<p> <strong> Список ролей бизнес-процесса: </strong> </p>
	<ul>
	<#list proc.getSwimlanes() as swimlane >
		<li> ${swimlane.getName()} </li>
	</#list>
	</ul>
	<br>
	
	<#-- VARIABLES -->
	<p> <strong> Cписок переменных бизнес-процесса: </strong> </p>
	<ul>
		<#list proc.getVariables(false,false,null) as var>
			<li> ${var.getName()} </li>
		</#list>
	</ul>
		
	<br/>
	</td>
	</tr>
	
	<tr bgcolor="#e3f4ff"><td><p><strong> Описание действий бизнес-процесса: </strong></p></td></tr>
			
	<#-- START POINT -->
	<#assign start = proc.getChildrenRecursive(model.start)?first >
	<tr><td><br/><p> Начало выполнения бизнес-процесса: ${start.getName()} <br/>
		<#if start.getSwimlane() ?? >
		 Роль: ${start.getSwimlane().getName()} <br/>
	</#if>
	<#assign afterStart = start.getLeavingTransitions()?first >
	Далее управление переходит к шагу <a href="#${afterStart.getTarget().getId()}">${afterStart.getTarget().getName()}</a></p>
	<br/></td></tr>
	
	<#-- NODES -->
	<#list proc.getChildren(model.node) as node>
			
		<#-- TaskState -->
		<#if node.class.simpleName == "TaskState" || node.class.simpleName == "Decision"  || node.class.simpleName == "Conjunction">
			<tr><td>
			<hr color="#e3f4ff"><br/>
			
				<#-- name -->
			<p id="${node.getId()}"> <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг: ${node.getName()} </strong> </font> </p>
			
			<#-- type -->
			<p>
			<#if node.class.simpleName == "Conjunction">
			Тип шага: Соединение<br/>
			<#elseif node.getLeavingTransitions()?size == 1>
				Тип шага: Действие<br/>
			<#else>
					Тип шага: Ветвление<br/>
			</#if>
			
			<#-- swimlane -->
				<#if node.class.simpleName == "TaskState" && node.getSwimlane() ?? >
				Роль: ${node.getSwimlane().getName()}<br/>
			</#if>
			
			<#-- transitions -->
				<#if node.getLeavingTransitions()?size == 1>
				<#assign afterTask = node.getLeavingTransitions()?first >
				Далее управление переходит к шагу <a href="#${afterTask.getTarget().getId()}">${afterTask.getTarget().getName()}</a></p>
			<#else>
					Далее управление переходит:</p>    
				<ul>
					<#list node.getLeavingTransitions() as transition>
							<p>в случае ${transition.getName()} <a href="#${transition.getTarget().getId()}">${transition.getTarget().getName()}</a> </p>
					</#list> 
				</ul>
		</#if>
				
			<#-- timer option -->
			<#if node.getTimer()?? >
				<#assign timer = node.getTimer() >
				<#if timer.getLeavingTransitions()?? && ( timer.getLeavingTransitions()?size > 0) >
						<#assign afterTimer = timer.getLeavingTransitions()?first>
					<#if timer.getDelay().hasDuration() >
						После истечения ${node.getPropertyValue("timerDelay").toString()} времени управление переходит к шагу 
					<#else>
						${timerDelay.toString()} времени управление переходит к шагу
					</#if>
						<a href="#${afterTimer.getTarget().getId()}">${afterTimer.getTarget().getName()}</a>
					</p>
				</#if>
				</#if>
		</#if>
		
			<#-- ParallelGateway -->
		<#if node.class.simpleName == "ParallelGateway" || node.class.simpleName == "Fork">
			<tr><td>
				<hr color="#e3f4ff"><br/>
	
				<#-- name -->
			<p id="${node.getId()}" > <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг: Параллельный шлюз ${node.getName()} </strong> </font> </p>
				
			<#-- type -->
				<#if ( node.getLeavingTransitions()?size > node.getArrivingTransitions()?size ) >
				<p>Тип шага: Разделение<br/>
			<#else>
				<p>Тип шага: Слияние<br/>
			</#if>
				
			<#-- transitions -->
			<#if node.getLeavingTransitions()?size == 1>
				<#assign afterTask = node.getLeavingTransitions()?first >
					Далее управление переходит к шагу <a href="#${afterTask.getTarget().getId()}">${afterTask.getTarget().getName()}</a></p>
			<#else>
				Далее управление переходит:</p>    
				<ul>
						<#list node.getLeavingTransitions() as transition>
						<p>в случае ${transition.getName()} <a href="#${transition.getTarget().getId()}">${transition.getTarget().getName()}</a> </p>
					</#list> 
					</ul>
			</#if>
		</#if>
		
		<#-- Join -->
		<#if node.class.simpleName == "Join">
				<tr><td>
			<hr color="#e3f4ff"><br/>
			
				<#-- name -->
			<p id="${node.getId()}" > <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг: Соединение ${node.getName()} </strong> </font> </p>
			
			<#-- type -->
			<p>Тип шага: Соединение<br/>
			
			<#-- transitions -->
			<#assign afterNode = node.getLeavingTransitions()?first >
			Далее cоединяются ${node.getArrivingTransitions()?size} точек управления, и управление переходит к шагу
			<a href="#${afterNode.getTarget().getId()}">${afterNode.getTarget().getName()}</a> 
			</p>    
		</#if>
		
		<#-- ExclusiveGateway -->
		<#if node.class.simpleName == "ExclusiveGateway">
				<tr><td>
			<hr color="#e3f4ff"><br/>
			
			<#-- name -->
			<p id="${node.getId()}" > <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг:  ${node.getName()} </strong> </font> </p>
			
			<#-- type -->
			<p>Тип шага: Исключающий шлюз<br/>
			
			<#-- transitions -->
				Далее управление переходит:</p>    
				<ul>
					<#list node.getLeavingTransitions() as transition>
					<p>в случае ${transition.getName()} <a href="#${transition.getTarget().getId()}">${transition.getTarget().getName()}</a> </p>
						</#list> 
				</ul>
			</p>    
			</#if>
		
		<#-- Timer -->
			<#if node.class.simpleName == "Timer" >
			<tr><td>
			<hr color="#e3f4ff"><br/>
			
				<#-- name -->
			<p id="${node.getId()}" > <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг: ${node.getName()} </strong> </font> </p>
			
			<#-- type -->
				<p>Тип шага: Таймер<br/>
			
			<#-- transitions -->
				<#assign afterNode = node.getLeavingTransitions()?first >
			<#assign timerDelay = node.getPropertyValue("timerDelay") >
			<#if timerDelay.hasDuration() >
				После истечения ${node.getPropertyValue("timerDelay").toString()} времени управление переходит к шагу 
			<#else>
				${timerDelay.toString()} времени управление переходит к шагу
				</#if>
				<a href="#${afterNode.getTarget().getId()}">${afterNode.getTarget().getName()}</a>
			</p>
		</#if>
			
		<#-- Receive & Send message -->
		<#if node.class.simpleName == "ReceiveMessageNode" || node.class.simpleName == "SendMessageNode">
			<tr><td>
				<hr color="#e3f4ff"><br/>
			
			<#-- name -->
			<p id="${node.getId()}" > <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг: ${node.getName()} </strong> </font> </p>
			
			<#-- type -->
				<p>
			<#if node.class.simpleName == "ReceiveMessageNode">
				Тип шага: Прием сообщения<br/>
			<#else>
				Тип шага: Отправка сообщения <br/>
			</#if>
				
			<#-- transitions -->
			<#assign afterNode = node.getLeavingTransitions()?first >
			<#if node.class.simpleName == "ReceiveMessageNode">
				После приема сообщения управление переходит к шагу
			<#else>
				После отправки сообщения управление переходит к шагу
				</#if>
				<a href="#${afterNode.getTarget().getId()}">${afterNode.getTarget().getName()}</a>
			</p>
			
			<#-- timer option -->
			<#if node.class.simpleName == "ReceiveMessageNode"  && node.getTimer()?? >
				<#assign timer = node.getTimer() >
				<#assign afterTimer = timer.getLeavingTransitions()?first>
				<p> 
						В случае задержки задания на ${timer.getPropertyValue("timerDelay").toString()} времени управление переходит к шагу 
					<a href="#${afterTimer.getTarget().getId()}">${afterTimer.getTarget().getName()}</a>
				</p>
			</#if>
			
			<#-- msg live time option -->
			<#if node.class.simpleName == "SendMessageNode" && node.getTtlDuration() ??>
				<p> Время жизни сообщения ${node.getTtlDuration().toString()}</p>
			</#if>
			</#if>
		
		<#-- Subprocess & Multisubprocess -->
		<#if node.class.simpleName == "Subprocess" || node.class.simpleName == "MultiSubprocess">
				<tr><td>
			<hr color="#e3f4ff"><br/>
			
			<#-- name -->
			<p id="${node.getId()}" > <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг: ${node.getName()} </strong> </font> </p>
				
			<#-- type -->
			<p>
			<#if node.class.simpleName == "Subprocess">
				Тип шага: Запуск подпроцесса<br/>
				<#else>
				Тип шага: Запуск мультиподпроцесса <br/>
			</#if>
			
				<#-- suprocess name -->
			<#if node.getSubProcessName() != "">
				<p> Имя подпроцесса ${node.getSubProcessName()} </p>
			</#if>
		</#if>
			
		<#-- Multi task state -->
		<#if node.class.simpleName == "MultiTaskState">
			<tr><td>
			<hr color="#e3f4ff"><br/>
			
				<#-- name -->
			<p id="${node.getId()}" > <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг: ${node.getName()} </strong> </font> </p>
			
			<#-- type -->
			<p>
				Тип шага: Запуск мультидействия <br/>
				</p>
			
			<#-- transitions -->
			<#if node.getLeavingTransitions()?size == 1>
				<#assign afterTask = node.getLeavingTransitions()?first >
					Далее управление переходит к шагу <a href="#${afterTask.getTarget().getId()}">${afterTask.getTarget().getName()}</a></p>
			<#else>
				Далее управление переходит:</p>    
				<ul>
					<#list node.getLeavingTransitions() as transition>
							<p>в случае ${transition.getName()} <a href="#${transition.getTarget().getId()}">${transition.getTarget().getName()}</a> </p>
				</#list> 
					</ul>
			</#if>	
			
		</#if>
		
			<#--  ScriptTask -->
		<#if node.class.simpleName == "ScriptTask">
			<tr><td>
			<hr color="#e3f4ff"><br/>
				
			<#-- name -->
			<p id="${node.getId()}" > <font color="#005D85" size="3" face="Verdana, Geneva, sans-serif"> <strong> Шаг: ${node.getName()} </strong> </font> </p>
			
			<#-- type -->
			<p>Тип шага: Выполнение сценария</p>
				
			<#-- transitions -->
			<#if node.getLeavingTransitions()?size == 1>
				<#assign afterTask = node.getLeavingTransitions()?first >
				Далее управление переходит к шагу <a href="#${afterTask.getTarget().getId()}">${afterTask.getTarget().getName()}</a></p>
			<#else>
					Далее управление переходит:</p>    
				<ul>
					<#list node.getLeavingTransitions() as transition>
							<p>в случае ${transition.getName()} <a href="#${transition.getTarget().getId()}">${transition.getTarget().getName()}</a> </p>
					</#list> 
				</ul>
			</#if>
		</#if>
			
		
		</td></tr>	
		
	
	</#list>
		
	<#-- EndTokenState -->
	<#list proc.getChildrenRecursive(model.endToken) as end>
		<tr><td><hr color="#e3f4ff">
			<p id="${end.getId()}"> Завершение потока  выполнения бизнес-процесса: ${end.getName()} </p>
		</td></tr>
	</#list>
	
		<#-- EndState -->
<#list proc.getChildrenRecursive(model.end) as end>
		<tr><td><hr color="#e3f4ff">
		<p id="${end.getId()}"> Завершение процесса выполнения бизнес-процесса: ${end.getName()} </p>
		</td></tr>
		</#list>
			
</table>