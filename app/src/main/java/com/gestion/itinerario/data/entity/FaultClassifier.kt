package com.gestion.itinerario.data.entity

object FaultClassifier {

    data class FaultSuggestion(
        val possibleCause: String,
        val suggestedType: ServiceType
    )

    private val rules: List<Pair<List<String>, FaultSuggestion>> = listOf(
        listOf("no enfr", "no fría", "no congela", "caliente", "calienta") to
            FaultSuggestion("Posible fuga de refrigerante o compresor defectuoso", ServiceType.REPAIR),
        listOf("hace ruido", "ruido", "vibra", "zumba", "traquea") to
            FaultSuggestion("Rodamientos desgastados o piezas sueltas internas", ServiceType.REPAIR),
        listOf("gotea", "fuga de agua", "derrama", "agua en el piso") to
            FaultSuggestion("Manguera de drenaje tapada o sello deteriorado", ServiceType.MAINTENANCE),
        listOf("no enciende", "no prende", "sin luz", "no arranca") to
            FaultSuggestion("Problema eléctrico: fusible, tablero o capacitor", ServiceType.REPAIR),
        listOf("no lava", "no centrifuga", "no gira", "lento") to
            FaultSuggestion("Motor debilitado, correa desgastada o rodamiento", ServiceType.REPAIR),
        listOf("olor", "quemado", "humo") to
            FaultSuggestion("Sobrecalentamiento: limpiar filtros y condensador urgente", ServiceType.MAINTENANCE),
        listOf("no enfría aire", "no da frío", "aire caliente") to
            FaultSuggestion("Carga de refrigerante baja o compresor a revisar", ServiceType.REPAIR),
        listOf("gotea por arriba", "gotea por el frente") to
            FaultSuggestion("Obstrucción en drenaje del evaporador", ServiceType.MAINTENANCE),
        listOf("mantenimiento", "limpieza", "servicio preventivo", "mantenimiento preventivo") to
            FaultSuggestion("Mantenimiento preventivo de rutina recomendado", ServiceType.MAINTENANCE),
        listOf("filtro", "sucio", "polvo", "suciedad") to
            FaultSuggestion("Limpieza de filtros y bobinas necesaria", ServiceType.MAINTENANCE),
        listOf("parpadea", "intermitente", "error", "código") to
            FaultSuggestion("Error en tablero de control o sensor defectuoso", ServiceType.REPAIR),
        listOf("instalación", "instalar", "nueva instalación") to
            FaultSuggestion("Instalación requerida — verificar conexiones y voltaje", ServiceType.INSTALLATION),
        listOf("nevera", "refrigerador") to
            FaultSuggestion("Diagnóstico completo de sistema de refrigeración", ServiceType.REPAIR),
        listOf("lavadora") to
            FaultSuggestion("Verificar motor, bomba y sistema de centrifugado", ServiceType.REPAIR),
        listOf("aire acondicionado", "split", "minisplit") to
            FaultSuggestion("Diagnóstico de sistema HVAC completo", ServiceType.MAINTENANCE)
    )

    fun classify(problem: String): FaultSuggestion? {
        if (problem.length < 4) return null
        val lower = problem.lowercase()
        return rules.firstOrNull { (keywords, _) ->
            keywords.any { kw -> kw in lower }
        }?.second
    }
}
