# Powers

Powers é um plugin Spigot para Minecraft para o servidor Kingdoms, 
criado para suportar novas mecânicas integrando a API `Event`
à linguagem `Lua`

---
Padrão de Scripts
---
##Todos os scripts devem recorrer a um mesmo padrão
Todos os scripts lidos pelo plugin devem recorrer a um
mesmo padrão, retornando uma tabela contendo o elemento
`"events"`.

O elemento `"events"` deve ser uma tabela, na qual
o índice de um elemento deve corresponder a uma subclasse
de `Event` na API Spigot, e o elemento deve ser
a função que será chamada no evento


```
function onSpigotEvent(event)
  -- Deal with event
end

script_table = {
  "events": {
    "SpigotEventName": onSpigotEvent,
  }
}

return script_table
```


###Aonde: 

- O nome de `onSpigotEvent` é reposto por um nome intuitivo
ao evento


- `-- Deal with event` é reposto pelo código em Lua
que altera o evento da maneira desejada, utilizando
a variável `event`


- `SpigotEventName` é reposto pelo nome da classe do evento
na API Spigot

---
Exemplos
---

### O Jogador ganha 5 níveis ao tomar dano

```
EntityType = class:fromName("org.bukkit.entity.EntityType")
Player = class:fromName("org.bukkit.entity.Player")

function onEntityDamage(event)
  if event:getEntityType():equals(EntityType.PLAYER) then
    entity = event:getEntity()
    player = class:cast(event:getEntity(), Player)
    player:setLevel(player:getLevel() + 1)
  end
end
            
            
script_table = {
  events = {
    EntityDamageEvent = onEntityDamage
  }
}
            
return script_table
```

---
###O Jogador perde 1 de vida (0.5 coração) ao consumir itens

```
function onPlayerItemConsume(event)
  event:getPlayer():damage(1)
end
            
            
script_table = {
  events = {
    PlayerItemConsumeEvent = onPlayerItemConsume
  }
}
            
return script_table
```
