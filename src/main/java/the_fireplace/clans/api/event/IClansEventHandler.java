package the_fireplace.clans.api.event;

public interface IClansEventHandler<T>
{
    T run(T event);
}
