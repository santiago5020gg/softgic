{{/* Nombre del Secret efectivo: existingSecret si se define, si no <release>-secret */}}
{{- define "plataforma.secretName" -}}
{{- if .Values.secret.existingSecret -}}
{{- .Values.secret.existingSecret -}}
{{- else -}}
{{- printf "%s-secret" .Release.Name -}}
{{- end -}}
{{- end -}}
