create table if not exists posts
(
    id         int auto_increment primary key,
    title      varchar(200) not null,
    content    text         null,
    created_at datetime     not null,
    updated_at datetime     not null
);

